package com.card.game.common.constant;

import lombok.Getter;

/**
 * 通用响应码枚举
 */
@Getter
public enum CodeEnum {

    // 成功
    SUCCESS(0, "成功"),

    // 客户端错误 (1xx)
    INVALID_PARAM(100, "参数错误"),
    MISSING_PARAM(101, "缺少必要参数"),
    PARAM_TYPE_ERROR(102, "参数类型错误"),

    // 认证错误 (2xx)
    TOKEN_MISSING(200, "缺少Token"),
    TOKEN_INVALID(201, "Token无效"),
    TOKEN_EXPIRED(202, "Token已过期"),
    UNAUTHORIZED(203, "未授权访问"),

    // 用户错误 (3xx)
    USER_NOT_FOUND(300, "用户不存在"),
    USER_EXISTS(301, "用户已存在"),
    WRONG_PASSWORD(302, "密码错误"),
    USER_BANNED(303, "用户已被封禁"),
    ALREADY_ONLINE(304, "用户已在线"),

    // 业务错误 (4xx)
    SESSION_NOT_FOUND(400, "会话不存在"),
    MESSAGE_HANDLE_FAILED(401, "消息处理失败"),
    GATEWAY_NOT_FOUND(402, "网关不存在"),
    AGENT_NOT_FOUND(403, "Agent不存在"),

    // 系统错误 (9xx)
    INTERNAL_ERROR(900, "服务器内部错误"),
    DB_ERROR(901, "数据库错误"),
    REDIS_ERROR(902, "缓存服务错误"),
    RPC_ERROR(903, "RPC调用失败"),
    TIMEOUT_ERROR(904, "请求超时"),

    // 未知错误
    UNKNOWN_ERROR(999, "未知错误");

    private final int code;
    private final String message;

    CodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static CodeEnum fromCode(int code) {
        for (CodeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return UNKNOWN_ERROR;
    }

    public static boolean isSuccess(int code) {
        return code == SUCCESS.code;
    }
}
