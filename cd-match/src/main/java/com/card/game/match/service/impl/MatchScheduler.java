package com.card.game.match.service.impl;

import com.card.game.common.client.GatewayGrpcClient;
import com.card.game.common.util.GameMessageUtils;
import com.card.game.common.util.IdGenerator;
import com.card.game.match.config.MatchConfig;
import com.card.game.match.constant.RedisKeysConstants;
import com.card.game.match.enums.GameTypeExtEnum;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MessageType;
import com.card.game.proto.gateway.PlayerGroup;
import com.card.game.proto.gateway.PlayerGroupType;
import com.card.game.proto.gateway.PushToPlayerGroupRequest;
import com.card.game.proto.match.MatchCommonResponse;
import com.card.game.proto.match.MatchStatus;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 匹配调度器
 * 定时扫描各游戏类型的匹配队列，将分数相近的玩家组成一局游戏。
 * 支持扩圈策略：等待越久，分数范围越大。
 * 当前支持的游戏类型：
 *   - dou_di_zhu（斗地主，3人/局）
 *   - jun_zheng（军争，8人/局）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchScheduler {

    @Resource
    private MatchQueueService matchQueueService;

    @Resource
    private MatchConfig matchConfig;

    @Resource
    private GatewayGrpcClient gatewayGrpcClient;

    @Resource
    private RedissonClient redissonClient;

   private static final Map<String, GameTypeExtEnum> GAME_TYPE_EXT_ENUM_MAP;

    private static final List<String> GAME_TYPE_KEYS ;

    static {
        GAME_TYPE_KEYS = GameTypeExtEnum.getAllGameTypesKeys();
        GAME_TYPE_EXT_ENUM_MAP=GameTypeExtEnum.getAllGameTypes().stream()
                        .collect(Collectors.toMap(
                                GameTypeExtEnum::getKey,
                                Function.identity()
                        ));
    }



    /**
     * 定时扫描所有游戏类型的匹配队列（默认每 2 秒执行一次）
     */
    @Scheduled(fixedDelayString = "#{@matchConfig.match.queueScanInterval}")
    public void scanAllQueues() {
        for (String gameTypeKey : GAME_TYPE_KEYS) {
            scanMatchQueue(gameTypeKey);
        }
    }

    /**
     * 扫描指定游戏类型的匹配队列
     */
    private void scanMatchQueue(String gameTypeKey) {
        GameTypeExtEnum gameTypeExtEnum = GAME_TYPE_EXT_ENUM_MAP.get(gameTypeKey);
        if (gameTypeExtEnum == null){
            log.error("[{}] 不支持的游戏类型", gameTypeKey);
            return;
        }
        int playersNeeded = gameTypeExtEnum.getPlayersNeeded();

        // 获取分布式锁，防止多实例并发匹配同一队列
        RLock lock = matchQueueService.getMatchLock(gameTypeKey);
        try {
            if (!lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                log.debug("[{}] 匹配锁被其他实例持有，跳过本次扫描", gameTypeKey);
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            int queueSize = matchQueueService.getQueueSize(gameTypeKey);
            if (queueSize < playersNeeded) {
                return;
            }

            // 获取所有在队列中的玩家
            Collection<String> allPlayers = matchQueueService.getAllPlayers(gameTypeKey);
            if (allPlayers.size() < playersNeeded) {
                return;
            }

            // 尝试匹配
            tryMatch(gameTypeKey, allPlayers, playersNeeded);

        } catch (Exception e) {
            log.error("[{}] 匹配扫描异常", gameTypeKey, e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 匹配超时清理：移除各游戏类型中超时的玩家
     */
    @Scheduled(fixedRate = 10000)
    public void cleanTimeoutPlayers() {
        for (String gameTypeKey : GAME_TYPE_KEYS) {
            try {
                Collection<String> allPlayers = matchQueueService.getAllPlayers(gameTypeKey);
                List<String> timeoutPlayers = new ArrayList<>();

                for (String playerId : allPlayers) {
                    long waitTime = matchQueueService.getWaitTimeSeconds(playerId, gameTypeKey);
                    if (waitTime >= matchConfig.getMatch().getMaxWaitSeconds()) {
                        timeoutPlayers.add(playerId);
                    }
                }

                if (!timeoutPlayers.isEmpty()) {
                    matchQueueService.removePlayers(gameTypeKey, timeoutPlayers);
                    log.info("[{}] 匹配超时移除: count={}, players={}",
                            gameTypeKey, timeoutPlayers.size(), timeoutPlayers);
                    PlayerGroup playerGroup = PlayerGroup.newBuilder()
                            .setType(PlayerGroupType.TEMP_GROUP)
                            .setGroupId(IdGenerator.getUUID("mt_tm"))
                            .addAllPlayerId(timeoutPlayers)
                            .build();
                    GameMessage message= GameMessageUtils.buildMessage(MessageType.MATCH_STATUS_QUERY_RESPONSE,null);
                    PushToPlayerGroupRequest request = PushToPlayerGroupRequest.newBuilder()
                            .setGroup(playerGroup)
                            .setMessage(message)
                            .build();
                    CommonResponse response = gatewayGrpcClient.pushToPlayerGroup(request);

                }
            } catch (Exception e) {
                log.error("[{}] 清理超时匹配异常", gameTypeKey, e);
            }
        }
    }

    /**
     * 核心匹配逻辑
     * 从队列中找出分数相近的 playersNeeded 人组成一局
     */
    private void tryMatch(String gameTypeKey, Collection<String> allPlayers,int playersNeeded) {
        MatchConfig.MatchConfigProps props = matchConfig.getMatch();

        int currentRange = props.getInitialScoreRange();
        int maxRange = props.getMaxScoreRange();

        // 逐步扩圈，直到找到足够玩家或达到最大范围
        while (currentRange <= maxRange) {
            Collection<String> matchedPlayers = findMatchWithinRange(gameTypeKey, allPlayers, currentRange, playersNeeded);

            if (matchedPlayers.size() >= playersNeeded) {
                handleMatchSuccess(gameTypeKey, matchedPlayers, playersNeeded);
                return;
            }

            currentRange += props.getScoreRangeExpandStep();
        }

        log.debug("[{}] 当前队列未匹配到足够玩家，队列大小={}, 需要={}", gameTypeKey, allPlayers.size(), playersNeeded);
    }

    /**
     * 在指定分数范围内尝试匹配
     */
    private Collection<String> findMatchWithinRange(String gameTypeKey, Collection<String> allPlayers, int range, int playersNeeded) {
        RScoredSortedSet<String> queue = redissonClient.getScoredSortedSet(RedisKeysConstants.MATCH_QUEUE_KEY + gameTypeKey);

        if (allPlayers.isEmpty()) {
            return List.of();
        }

        // 从最高分玩家开始，以该玩家分数为基准在 ±range 内找齐 playersNeeded 人
        for (String basePlayer : allPlayers) {
            Double baseScore = queue.getScore(basePlayer);
            if (baseScore == null) continue;

            double minScore = baseScore - range;
            double maxScore = baseScore + range;

            Collection<String> candidates = matchQueueService.getPlayersByScoreRange(
                    gameTypeKey, minScore, maxScore, playersNeeded);

            if (candidates.size() >= playersNeeded) {
                return candidates;
            }
        }

        return List.of();
    }

    /**
     * 匹配成功处理
     */
    private void handleMatchSuccess(String gameTypeKey, Collection<String> players, int playersNeeded) {
        List<String> playerList = new ArrayList<>(players).subList(0,
                Math.min(players.size(), playersNeeded));

        // 从队列中移除
        matchQueueService.removePlayers(gameTypeKey, playerList);

        log.info("[{}] 匹配成功: count={}, players={}", gameTypeKey, playerList.size(), playerList);
        // TODO: 调用 Game 服务的 CreateRoom RPC（需注入 GameGrpcClient） 得到roomID
        //  成功后向每个玩家推送 MATCH_SUCCESS_RESPONSE
        //  失败则重新入队（重试最多 3 次）
        PlayerGroup playerGroup = PlayerGroup.newBuilder()
                .setType(PlayerGroupType.TEMP_GROUP)
                .setGroupId(IdGenerator.getUUID("mt_tm"))//应该用游戏房间id
                .addAllPlayerId(playerList)
                .build();
        MatchCommonResponse matchSuccessResponse = MatchCommonResponse.newBuilder()
                .setStatus(MatchStatus.SUCCESS)
                .setRoomId("")
                .build();
        ByteString matchSuccessResponseByteString = matchSuccessResponse.toByteString();
        GameMessage message= GameMessageUtils.buildMessage(MessageType.MATCH_STATUS_QUERY_RESPONSE,matchSuccessResponseByteString);
        PushToPlayerGroupRequest request = PushToPlayerGroupRequest.newBuilder()
                .setGroup(playerGroup)
                .setMessage(message)
                .build();
        gatewayGrpcClient.pushToPlayerGroup(request);
    }
}
