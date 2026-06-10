package com.card.game.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Agent 服务启动类
 *
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@ComponentScan(basePackages = {"com.card.game"})
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
        log.info("========================================");
        log.info("    Agent Service Started Successfully   ");
        log.info("========================================");
    }
}
