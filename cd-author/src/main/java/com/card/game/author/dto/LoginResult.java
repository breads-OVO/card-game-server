package com.card.game.author.dto;

import com.card.game.author.entity.AccountEntity;
import com.card.game.proto.common.Code;
import lombok.Data;

@Data
public class LoginResult {

    private final Code code;
    private final String message;
    private final String token;
    private final Long expireAt;
    private final AccountEntity account;
    private final boolean isReconnect;

    public LoginResult(Code code, String message, String token, Long expireAt,
                       AccountEntity account, boolean isReconnect) {
        this.code = code;
        this.message = message;
        this.token = token;
        this.expireAt = expireAt;
        this.account = account;
        this.isReconnect = isReconnect;
    }

    public static LoginResult success(String token, Long expireAt, AccountEntity account) {
        return new LoginResult(Code.SUCCESS, "登录成功", token, expireAt, account, false);
    }

    public static LoginResult reconnect(String token, Long expireAt, AccountEntity account) {
        return new LoginResult(Code.SUCCESS, "重连成功", token, expireAt, account, true);
    }

    public static LoginResult failure(Code code, String message) {
        return new LoginResult(code, message, null, null, null, false);
    }
}
