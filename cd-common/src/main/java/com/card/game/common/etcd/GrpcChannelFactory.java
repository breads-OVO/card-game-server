package com.card.game.common.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.GetOption;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GrpcChannelFactory {

    private final Client etcdClient;
    private final Map<String, ManagedChannel> channelCache = new ConcurrentHashMap<>();

    public GrpcChannelFactory(Client etcdClient) {
        this.etcdClient = etcdClient;
    }

    public Channel getChannel(String serviceName) {
        return channelCache.computeIfAbsent(serviceName, name -> {
            log.info("创建 gRPC 通道: serviceName={}", name);

            String address = resolveServiceAddress(name);
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .build();

            log.info("gRPC 通道创建成功: {} -> {}:{}", name, host, port);
            return channel;
        });
    }

    private String resolveServiceAddress(String serviceName) {
        ByteSequence prefix = ByteSequence.from(
                String.format("/services/game/%s/", serviceName).getBytes(StandardCharsets.UTF_8)
        );
        log.info("查询 etcd 前缀: /services/game/{}/", serviceName);
        Exception lastException = null;
        for (int i = 0; i < 3; i++) {
            try {
                if (i > 0) {
                    Thread.sleep(2000L * i);
                }
                var response = etcdClient.getKVClient().get(prefix,
                        GetOption.newBuilder().withPrefix(prefix).build()).get(5, TimeUnit.SECONDS);
                var kvs = response.getKvs();
                log.info("etcd 查询结果: count={}, more={}", kvs.size(), response.isMore());
                if (!kvs.isEmpty()) {
                    for (var kv : kvs) {
                        log.info("  etcd key: {}, value: {}",
                                new String(kv.getKey().getBytes(), StandardCharsets.UTF_8),
                                new String(kv.getValue().getBytes(), StandardCharsets.UTF_8));
                    }
                    String value = new String(kvs.get(0).getValue().getBytes(), StandardCharsets.UTF_8);
                    log.info("从 etcd 解析服务地址: {} -> {}", serviceName, value);
                    return value;
                }
                log.warn("第 {} 次查询 etcd 未找到服务: {}", i + 1, serviceName);
            } catch (Exception e) {
                lastException = e;
                log.warn("第 {} 次查询 etcd 失败: {}", i + 1, e.getMessage());
            }
        }
        throw new RuntimeException("从 etcd 解析服务地址失败: " + serviceName, lastException);
    }

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
