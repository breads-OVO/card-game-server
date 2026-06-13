package com.card.game.match;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Match 服务启动类
 * 处理玩家匹配逻辑、匹配队列管理、段位分更新
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@ComponentScan(basePackages = {"com.card.game"})
public class MatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchApplication.class, args);
        log.info("========================================");
        log.info("   Match Server Started Successfully   ");
        log.info("========================================");
    }
}
