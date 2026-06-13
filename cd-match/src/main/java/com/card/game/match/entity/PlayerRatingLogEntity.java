package com.card.game.match.entity;

import com.card.game.common.db.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 玩家段位分变更流水表
 * 记录每次对局后的段位分变化
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "player_rating_log", indexes = {
        @Index(name = "idx_player_id", columnList = "playerId"),
        @Index(name = "idx_game_round_id", columnList = "gameRoundId", unique = true)
})
public class PlayerRatingLogEntity extends BaseEntity {

    /** 玩家 ID */
    @Column(nullable = false, length = 64)
    private String playerId;

    /** 变更前分数 */
    @Column(nullable = false)
    private int oldScore;

    /** 变更后分数 */
    @Column(nullable = false)
    private int newScore;

    /** 分数变化（正数为增加，负数为减少） */
    @Column(nullable = false)
    private int delta;

    /** 是否为胜利方 */
    @Column(nullable = false)
    private boolean win;

    /** 对局 ID（幂等键） */
    @Column(nullable = false, length = 64, unique = true)
    private String gameRoundId;

}
