package com.card.game.common.config;

import com.card.game.common.client.AgentGrpcClient;
import com.card.game.common.client.AuthorGrpcClient;
import com.card.game.common.client.GatewayGrpcClient;
import com.card.game.common.client.MatchGrpcClient;
import com.card.game.common.etcd.GrpcChannelFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GrpcClientConfig {

    @Bean
    public AuthorGrpcClient authorGrpcClient(GrpcChannelFactory channelFactory) {
        log.info("创建 Author gRPC 客户端（懒加载，调用时才开始发现服务）");
        return new AuthorGrpcClient(channelFactory, "author-server");
    }

    @Bean
    public AgentGrpcClient agentGrpcClient(GrpcChannelFactory channelFactory) {
        log.info("创建 Agent gRPC 客户端（懒加载，调用时才开始发现服务）");
        return new AgentGrpcClient(channelFactory, "agent-server");
    }

    @Bean
    public GatewayGrpcClient gatewayGrpcClient(GrpcChannelFactory channelFactory) {
        log.info("创建 Gateway gRPC 客户端（懒加载，调用时才开始发现服务）");
        return new GatewayGrpcClient(channelFactory, "gateway-server");
    }

    @Bean
    public MatchGrpcClient matchGrpcClient(GrpcChannelFactory channelFactory) {
        log.info("创建 Match gRPC 客户端（懒加载，调用时才开始发现服务）");
        return new MatchGrpcClient(channelFactory, "match-server");
    }
}

