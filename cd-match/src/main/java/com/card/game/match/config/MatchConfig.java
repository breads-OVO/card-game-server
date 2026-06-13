package com.card.game.match.config;

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
public class MatchConfig {

    /** 服务名称 */
    private String name;

    /** 服务版本 */
    private String version;

    /** 环境标识: dev, test, prod */
    private String env;

    /** 匹配配置 */
    private MatchConfigProps match = new MatchConfigProps();

    /** 线程池配置 */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    @Data
    public static class MatchConfigProps {
        /** 队列扫描间隔（毫秒） */
        private int queueScanInterval = 2000;

        /** 最长匹配等待时间（秒） */
        private int maxWaitSeconds = 120;

        /** 初始段位分范围 */
        private int initialScoreRange = 200;

        /** 每次扩圈增加分数 */
        private int scoreRangeExpandStep = 100;

        /** 扩圈间隔（秒） */
        private int scoreRangeExpandInterval = 10;

        /** 创建房间重试次数 */
        private int gameCreateRetryCount = 3;

        /** 新玩家初始段位分 */
        private int defaultRating = 1200;

        /** 最大扩圈范围 */
        private int maxScoreRange = 1000;

        /** ELO K 值 */
        private int eloKFactor = 32;
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
        private String threadNamePrefix = "match-worker-";
    }
}
