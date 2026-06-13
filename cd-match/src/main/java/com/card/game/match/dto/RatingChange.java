package com.card.game.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 段位分变更数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class RatingChange {
    private String playerId;
    private int oldScore;
    private double opponentAvgScore;
    private boolean win;
    private String gameRoundId;
}
