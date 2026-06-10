package com.card.game.common.etcd;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC 通道工厂
 * 基于 etcd 服务发现创建 gRPC 客户端连接
 */
@Slf4j
@Component
public class GrpcChannelFactory {

    private final Map<String, ManagedChannel> channelCache = new ConcurrentHashMap<>();

    /**
     * 获取服务通道（带负载均衡）
     * @param serviceName 服务名称
     * @return gRPC Channel
     */
    public Channel getChannel(String serviceName) {
        return channelCache.computeIfAbsent(serviceName, name -> {
            log.info("创建 gRPC 通道: serviceName={}", name);

            // 使用 discovery:// 协议，自动通过 SPI 找到 EtcdNameResolverProvider
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget("discovery://" + name)
                    .usePlaintext()
                    .build();

            log.info("gRPC 通道创建成功: {}", name);
            return channel;
        });
    }

    /**
     * 关闭所有通道
     */
    public void shutdown() {
        channelCache.values().forEach(channel -> {
            try {
                channel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        channelCache.clear();
        log.info("所有 gRPC 通道已关闭");
    }
}
