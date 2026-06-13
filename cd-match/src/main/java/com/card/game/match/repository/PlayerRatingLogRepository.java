package com.card.game.match.repository;

import com.card.game.match.entity.PlayerRatingLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 段位分变更流水仓储
 */
@Repository
public interface PlayerRatingLogRepository extends JpaRepository<PlayerRatingLogEntity, String> {

    /**
     * 按 gameRoundId 查询（幂等校验用）
     */
    Optional<PlayerRatingLogEntity> findByGameRoundId(String gameRoundId);

    /**
     * 按玩家 ID 查询最近记录
     */
   List<PlayerRatingLogEntity> findTop10ByPlayerIdOrderByCreateTimeDesc(String playerId);
}
