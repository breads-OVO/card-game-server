package com.card.game.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Gateway 服务启动类
 *
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.card.game")
@EnableConfigurationProperties
@EnableScheduling
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        log.info("========================================");
        log.info("   Gateway Service Started Successfully   ");
        log.info("========================================");
    }
}
