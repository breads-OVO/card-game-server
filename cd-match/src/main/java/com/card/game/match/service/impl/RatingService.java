package com.card.game.match.service.impl;

import com.card.game.match.config.MatchConfig;
import com.card.game.match.constant.RedisKeysConstants;
import com.card.game.match.dto.RatingChange;
import com.card.game.match.entity.PlayerRatingLogEntity;
import com.card.game.match.repository.PlayerRatingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 段位分管理服务
 * 使用简化 ELO 算法计算并更新玩家段位分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    @Resource
    private  RedissonClient redissonClient;
    @Resource
    private final PlayerRatingLogRepository ratingLogRepository;
    @Resource
    private final MatchConfig matchConfig;

    /**
     * 批量更新段位分（幂等）
     *
     * @param changes    段位分变更列表
     * @param gameRoundId 对局 ID（幂等键）
     * @return true-更新成功, false-已处理过（幂等）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRatings(List<RatingChange> changes, String gameRoundId) {
        // 幂等校验：已处理过的 gameRoundId 直接返回
        RBucket<Boolean> doneBucket = redissonClient.getBucket(RedisKeysConstants.RATING_DONE_KEY + gameRoundId);
        if (doneBucket.isExists()) {
            log.info("段位分更新幂等跳过: gameRoundId={}", gameRoundId);
            return false;
        }

        List<PlayerRatingLogEntity> logs = changes.stream()
                .map(this::calculateElo)
                .collect(Collectors.toList());

        ratingLogRepository.saveAll(logs);

        doneBucket.set(true, 24, TimeUnit.HOURS);

        log.info("段位分更新完成: gameRoundId={}, count={}", gameRoundId, changes.size());
        return true;
    }

    /**
     * 简化 ELO 计算
     * newScore = oldScore + K * (actual - expected)
     * expected = 1 / (1 + 10^((opponentAvg - myScore) / 400))
     */
    private PlayerRatingLogEntity calculateElo(RatingChange change) {
        double opponentAvg = change.getOpponentAvgScore();
        double expected = 1.0 / (1.0 + Math.pow(10, (opponentAvg - change.getOldScore()) / 400));
        int kFactor = matchConfig.getMatch().getEloKFactor();
        int delta = (int) Math.round(kFactor * (change.isWin() ? 1 : 0 - expected));
        int newScore = Math.max(0, change.getOldScore() + delta);

        PlayerRatingLogEntity logEntity = new PlayerRatingLogEntity();
        logEntity.setPlayerId(change.getPlayerId());
        logEntity.setOldScore(change.getOldScore());
        logEntity.setNewScore(newScore);
        logEntity.setDelta(delta);
        logEntity.setGameRoundId(change.getGameRoundId());
        logEntity.setWin(change.isWin());

        return logEntity;
    }

}
