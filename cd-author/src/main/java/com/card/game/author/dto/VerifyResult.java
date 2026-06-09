package com.card.game.author.dto;

import com.card.game.proto.common.Code;
import lombok.Data;

@Data
public class VerifyResult {

    private final Code code;
    private final String message;
    private final String playerId;
    private final String username;
    private final Long expireAt;

    public VerifyResult(Code code, String message, String playerId, String username, Long expireAt) {
        this.code = code;
        this.message = message;
        this.playerId = playerId;
        this.username = username;
        this.expireAt = expireAt;
    }

    public static VerifyResult success(String playerId, String username, Long expireAt) {
        return new VerifyResult(Code.SUCCESS, "Token有效", playerId, username, expireAt);
    }

    public static VerifyResult failure(Code code, String message) {
        return new VerifyResult(code, message, null, null, null);
    }
}
