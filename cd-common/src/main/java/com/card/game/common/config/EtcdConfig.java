package com.card.game.common.config;

import com.card.game.common.etcd.EtcdNameResolverProvider;
import io.etcd.jetcd.Client;
import io.grpc.NameResolverRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EtcdConfig {

    @Value("${etcd.endpoints}")
    private String endpoints;

    @Bean
    public Client etcdClient() {
        return Client.builder()
                .endpoints(endpoints.split(","))
                .build();
    }

    /**
     * 注册 etcd gRPC 服务发现
     * 通过方法参数注入 etcdClient，Spring 会自动处理依赖顺序
     */
    @Bean
    public EtcdNameResolverProvider etcdNameResolverProvider(Client etcdClient) {
        EtcdNameResolverProvider provider = new EtcdNameResolverProvider(etcdClient);
        NameResolverRegistry.getDefaultRegistry().register(provider);
        log.info("✅ etcd gRPC 服务发现已注册 (discovery://)");
        return provider;
    }
}
