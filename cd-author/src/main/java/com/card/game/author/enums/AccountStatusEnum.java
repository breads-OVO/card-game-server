package com.card.game.author.enums;

import lombok.Getter;

@Getter
public enum AccountStatusEnum {
    STATUS_NORMAL(0, "正常"),
    STATUS_BANNED(1, "封禁"),
    STATUS_DELETED(2, "删除");
    private final Integer code;
    private final String message;
    AccountStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
