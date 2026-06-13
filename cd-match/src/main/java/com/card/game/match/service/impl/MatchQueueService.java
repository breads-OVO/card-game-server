package com.card.game.match.service.impl;

import com.card.game.common.client.GatewayGrpcClient;
import com.card.game.match.config.MatchConfig;
import com.card.game.match.constant.RedisKeysConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 匹配队列管理服务
 * 基于 Redis Sorted Set 实现匹配队列，支持多实例并发安全
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchQueueService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MatchConfig matchConfig;

    /**
     * 将玩家加入匹配队列
     *
     * @param playerId 玩家 ID
     * @param gameType 游戏模式
     * @param rating   玩家段位分
     */
    public void enqueue(String playerId, String gameType, int rating) {
        RScoredSortedSet<String> queue = getQueue(gameType);
        queue.add(rating, playerId);
        // 记录玩家入队时间
        redissonClient.getBucket(RedisKeysConstants.MATCH_PLAYER_KEY + playerId + ":" + gameType)
                .set(System.currentTimeMillis(), matchConfig.getMatch().getMaxWaitSeconds(), TimeUnit.SECONDS);
        log.debug("玩家入队: playerId={}, gameType={}, rating={}", playerId, gameType, rating);
    }

    /**
     * 将玩家从匹配队列移除
     *
     * @param playerId 玩家 ID
     * @param gameType 游戏模式
     * @return 是否成功移除
     */
    public boolean dequeue(String playerId, String gameType) {
        RScoredSortedSet<String> queue = getQueue(gameType);
        boolean removed = queue.remove(playerId);
        redissonClient.getBucket(RedisKeysConstants.MATCH_PLAYER_KEY + playerId + ":" + gameType).delete();
        if (removed) {
            log.debug("玩家出队: playerId={}, gameType={}", playerId, gameType);
        }
        return removed;
    }

    /**
     * 检查玩家是否已在匹配队列中
     */
    public boolean isInQueue(String playerId, String gameType) {
        return getQueue(gameType).contains(playerId);
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize(String gameType) {
        return getQueue(gameType).size();
    }

    /**
     * 获取玩家已等待时间（秒）
     *
     * @return 等待秒数，不在队列中返回 -1
     */
    public long getWaitTimeSeconds(String playerId, String gameType) {
        Long enqueueTime = (Long) redissonClient.getBucket(RedisKeysConstants.MATCH_PLAYER_KEY + playerId + ":" + gameType).get();
        if (enqueueTime == null) {
            return -1;
        }
        return (System.currentTimeMillis() - enqueueTime) / 1000;
    }

    /**
     * 获取指定分数范围内的玩家列表
     *
     * @param gameType   游戏模式
     * @param minScore   最小分数
     * @param maxScore   最大分数
     * @param count      最多返回人数
     * @return 玩家 ID 集合
     */
    public Collection<String> getPlayersByScoreRange(String gameType, double minScore, double maxScore, int count) {
        RScoredSortedSet<String> queue = getQueue(gameType);
        // score 升序排列，取 [minScore, maxScore] 范围内的前 count 个
        return queue.valueRange(minScore, true, maxScore, true, 0, count);
    }

    /**
     * 批量从队列移除玩家
     */
    public void removePlayers(String gameType, Collection<String> playerIds) {
        RScoredSortedSet<String> queue = getQueue(gameType);
        queue.removeAll(playerIds);
        for (String playerId : playerIds) {
            redissonClient.getBucket(RedisKeysConstants.MATCH_PLAYER_KEY + playerId + ":" + gameType).delete();
        }
        log.debug("批量移除玩家: gameType={}, count={}", gameType, playerIds.size());
    }

    /**
     * 批量将玩家重新加入队列（匹配失败回滚）
     */
    public void reenqueuePlayers(String gameType, Collection<String> playerIds) {
        RScoredSortedSet<String> queue = getQueue(gameType);
        for (String playerId : playerIds) {
            Double score = queue.getScore(playerId);
            if (score != null) {
                queue.add(score, playerId);
            }
        }
        log.debug("批量重新入队: gameType={}, count={}", gameType, playerIds.size());
    }

    /**
     * 获取匹配分布式锁
     */
    public RLock getMatchLock(String gameType) {
        return redissonClient.getLock(RedisKeysConstants.MATCH_LOCK_KEY + gameType);
    }

    /**
     * 获取队列中所有玩家及其分数
     */
    public Collection<String> getAllPlayers(String gameType) {
        return getQueue(gameType).readAll();
    }

    private RScoredSortedSet<String> getQueue(String gameType) {
        return redissonClient.getScoredSortedSet(RedisKeysConstants.MATCH_QUEUE_KEY + gameType);
    }
}
