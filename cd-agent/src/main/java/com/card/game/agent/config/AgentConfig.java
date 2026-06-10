package com.card.game.agent.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置类
 * 对应 application.yml 中的 app 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AgentConfig {

    /** 服务名称 */
    private String name ;

    /** 服务版本 */
    private String version ;

    /** 环境标识: dev, test, prod */
    private String env ;

    /** 服务实例ID（用于区分多实例） */
    private String instanceId ;

    /** 会话配置 */
    private SessionConfig session = new SessionConfig();

    /** 线程池配置 */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    @Data
    public static class SessionConfig {
        /** 会话过期时间（秒） */
        private int expireSeconds = 90;

        /** 心跳超时时间（秒） */
        private int heartbeatTimeoutSeconds = 90;

        /** 最大在线玩家数 */
        private int maxOnlinePlayers = 100000;
    }

    @Data
    public static class ThreadPoolConfig {
        /** 核心线程数 */
        private int corePoolSize = 8;

        /** 最大线程数 */
        private int maxPoolSize = 32;

        /** 队列容量 */
        private int queueCapacity = 1000;

        /** 线程名前缀 */
        private String threadNamePrefix = "agent-worker-";
    }
}