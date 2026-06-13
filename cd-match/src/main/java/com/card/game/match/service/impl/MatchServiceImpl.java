package com.card.game.match.service.impl;

import com.card.game.match.enums.GameTypeExtEnum;
import com.card.game.match.service.MatchService;
import com.card.game.proto.match.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Resource
    private MatchQueueService matchQueueService;

    /**
     * 通用匹配
     */
    @Override
    public MatchCommonResponse matchCommon(MatchCommonRequest request) {
        GameTypeExtEnum gameType = GameTypeExtEnum.getByGameType(request.getGameType());
        String playerId = request.getPlayerId();
        String gameTypeKey = gameType.getKey();
        try {
            matchQueueService.enqueue(playerId, gameTypeKey, 100);
            log.info("玩家 {} 加入 {} 匹配队列", playerId, gameTypeKey);
            return MatchCommonResponse.newBuilder()
                    .setStatus(MatchStatus.MATCHING)
                    .build();
        }catch (Exception e){
            log.error("玩家 {} 匹配 {} 失败", playerId, gameTypeKey, e);
            return MatchCommonResponse.newBuilder()
                    .setStatus(MatchStatus.FAIL)
                    .build();
        }
    }
}
