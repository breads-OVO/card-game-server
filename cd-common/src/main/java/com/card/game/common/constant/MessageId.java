package com.card.game.common.constant;

/**
 * 消息ID定义（客户端与网关通信）
 */
public final class MessageId {

    private MessageId() {}

    // ==================== 认证消息 (1000-1999) ====================

    /** 登录请求/响应 */
    public static final int LOGIN_REQ = 1001;
    public static final int LOGIN_RSP = 1002;

    /** 登出请求/响应 */
    public static final int LOGOUT_REQ = 1003;
    public static final int LOGOUT_RSP = 1004;

    /** 心跳请求/响应 */
    public static final int HEARTBEAT_REQ = 1005;
    public static final int HEARTBEAT_RSP = 1006;

    /** 重连请求/响应 */
    public static final int RECONNECT_REQ = 1007;
    public static final int RECONNECT_RSP = 1008;

    // ==================== 业务消息 (2000-9999) ====================

    /** 业务消息起始 */
    public static final int BUSINESS_MSG_START = 2000;

    /** 通用业务请求/响应 */
    public static final int BUSINESS_REQ = 2001;
    public static final int BUSINESS_RSP = 2002;

    /** 玩家信息请求/响应 */
    public static final int PLAYER_INFO_REQ = 2101;
    public static final int PLAYER_INFO_RSP = 2102;
}
