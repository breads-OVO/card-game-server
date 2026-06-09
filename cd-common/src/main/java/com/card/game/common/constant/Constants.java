package com.card.game.common.constant;

/**
 * 全局常量定义
 */
public final class Constants {

    private Constants() {}

    // ==================== 系统配置 ====================

    /** 默认字符集 */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /** 默认时区 */
    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    /** 日期时间格式 */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // ==================== 网络配置 ====================

    /** 默认端口 */
    public static final int DEFAULT_GATEWAY_PORT = 8080;
    public static final int DEFAULT_LOGIN_PORT = 8090;
    public static final int DEFAULT_AGENT_PORT = 8092;

    /** 心跳间隔（秒） */
    public static final int HEARTBEAT_INTERVAL_SEC = 30;

    /** 空闲超时（秒） */
    public static final int IDLE_TIMEOUT_SEC = 90;

    /** 最大消息长度（字节） */
    public static final int MAX_FRAME_LENGTH = 65536;

    // ==================== 缓存配置 ====================

    /** Token 有效期（天） */
    public static final int TOKEN_EXPIRE_DAYS = 7;

    /** 会话过期时间（秒） */
    public static final int SESSION_EXPIRE_SEC = 90;

    // ==================== 线程池配置 ====================

    /** 默认核心线程数 */
    public static final int DEFAULT_CORE_POOL_SIZE = 4;

    /** 默认最大线程数 */
    public static final int DEFAULT_MAX_POOL_SIZE = 16;

    /** 默认队列大小 */
    public static final int DEFAULT_QUEUE_SIZE = 1000;

    // ==================== Redis Key 前缀 ====================

    /** Token 存储前缀 */
    public static final String REDIS_KEY_TOKEN = "game:token:";

    /** 会话映射前缀 */
    public static final String REDIS_KEY_SESSION = "game:session:";

    /** 在线玩家前缀 */
    public static final String REDIS_KEY_ONLINE = "game:online:";
}