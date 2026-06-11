package com.card.game.author;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * author-server 服务启动类
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.card.game")
@EnableConfigurationProperties
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.card.game.author.repository")
public class AuthorApplication  {
    public static void main(String[] args) {
        SpringApplication.run(AuthorApplication.class, args);
        log.info("========================================");
        log.info("   author-server Started Successfully   ");
        log.info("========================================");
    }
}