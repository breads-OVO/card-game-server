package com.card.game.author.config;


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
public class AuthorConfig {

    /** 服务名称 */
    private String name = "game-login";

    /** 服务版本 */
    private String version = "1.0.0";

    /** 环境标识: dev, test, prod */
    private String env = "dev";

    /** JWT 配置 */
    private JwtConfig jwt = new JwtConfig();

    /** 线程池配置 */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    @Data
    public static class JwtConfig {
        /** JWT 密钥 */
        private String secret = "game-login-secret-key-2024";

        /** Token 有效期（天） */
        private int expireDays = 7;

        /** Token 刷新提前量（小时） */
        private int refreshHours = 24;
    }

    @Data
    public static class ThreadPoolConfig {
        /** 核心线程数 */
        private int corePoolSize = 4;

        /** 最大线程数 */
        private int maxPoolSize = 16;

        /** 队列容量 */
        private int queueCapacity = 500;

        /** 线程名前缀 */
        private String threadNamePrefix = "login-worker-";
    }
}