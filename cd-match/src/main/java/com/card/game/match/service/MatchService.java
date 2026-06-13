package com.card.game.match.service;

import com.card.game.proto.match.MatchCommonRequest;
import com.card.game.proto.match.MatchCommonResponse;

public interface MatchService {

    /**
     * 通用匹配
     */
    MatchCommonResponse matchCommon(MatchCommonRequest request);
}
