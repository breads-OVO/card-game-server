package com.card.game.common.exception;

import lombok.Getter;

/**
 * 错误码定义（结构化）
 */
@Getter
public enum ErrorCode {

    // 通用错误
    SUCCESS("0000", "成功"),
    UNKNOWN_ERROR("9999", "未知错误"),

    // 参数错误 (PARAM-XXXX)
    PARAM_INVALID("PARAM-0001", "参数无效"),
    PARAM_MISSING("PARAM-0002", "缺少必要参数"),

    // 业务错误 (BIZ-XXXX)
    USER_NOT_FOUND("BIZ-1001", "用户不存在"),
    USER_EXISTS("BIZ-1002", "用户已存在"),
    WRONG_PASSWORD("BIZ-1003", "密码错误"),

    // 系统错误 (SYS-XXXX)
    DB_ERROR("SYS-2001", "数据库异常"),
    REDIS_ERROR("SYS-2002", "缓存异常"),
    RPC_ERROR("SYS-2003", "RPC调用异常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}