package com.card.game.common.config;

import com.card.game.common.client.AuthorGrpcClient;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 *r gRPC 客户端配置
 */
@Slf4j
@Configuration
@DependsOn("etcdNameResolverProvider")
public class GrpcClientConfig {

    /**
     * 创建 AuthorGrpcClient Bean
     * @param authorChannel 由 grpc-spring-boot-starter 自动注入的 Channel
     * @return AuthorGrpcClient 实例
     */
    @Bean
    public AuthorGrpcClient authorGrpcClient(@GrpcClient("author-service") Channel authorChannel) {
        log.info("创建 Author gRPC 客户端");
        return new AuthorGrpcClient(authorChannel);
    }
}

