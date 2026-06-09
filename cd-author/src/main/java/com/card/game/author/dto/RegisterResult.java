package com.card.game.author.dto;

import com.card.game.proto.common.Code;
import lombok.Data;

@Data
public class RegisterResult {

    private final Code code;
    private final String message;
    private final String playerId;
    private final Long createTime;

    public RegisterResult(Code code, String message, String playerId, Long createTime) {
        this.code = code;
        this.message = message;
        this.playerId = playerId;
        this.createTime = createTime;
    }

    public static RegisterResult success(String playerId, Long createTime) {
        return new RegisterResult(Code.SUCCESS, "注册成功", playerId, createTime);
    }

    public static RegisterResult failure(Code code, String message) {
        return new RegisterResult(code, message, null, null);
    }
}
