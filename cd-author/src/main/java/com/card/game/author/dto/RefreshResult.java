package com.card.game.author.dto;

import com.card.game.proto.common.Code;
import lombok.Data;

@Data
public class RefreshResult {

    private final Code code;
    private final String message;
    private final String newToken;
    private final Long expireAt;

    public RefreshResult(Code code, String message, String newToken, Long expireAt) {
        this.code = code;
        this.message = message;
        this.newToken = newToken;
        this.expireAt = expireAt;
    }

    public static RefreshResult success(String newToken, Long expireAt) {
        return new RefreshResult(Code.SUCCESS, "刷新成功", newToken, expireAt);
    }

    public static RefreshResult failure(Code code, String message) {
        return new RefreshResult(code, message, null, null);
    }
}

