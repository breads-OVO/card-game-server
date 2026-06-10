package com.card.game.common.etcd;

import io.etcd.jetcd.ByteSequence;
import io.grpc.*;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.watch.WatchEvent;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 etcd 的 NameResolver
 */
public class EtcdNameResolver extends NameResolver {
    private final String serviceName;
    private final Client etcdClient;
    private Listener2 listener;
    private boolean resolving = false;

    public EtcdNameResolver(String serviceName, Client etcdClient) {
        this.serviceName = serviceName;
        this.etcdClient = etcdClient;
    }

    @Override
    public String getServiceAuthority() {
        return serviceName;
    }

    @Override
    public void start(Listener2 listener) {
        this.listener = listener;
        resolve();                 // 首次解析
        watchServiceChanges();     // 监听变更
    }

    /** 从 etcd 拉取服务地址列表 */
    private void resolve() {
        if (resolving) return;
        resolving = true;

        try {
            ByteSequence prefix = ByteSequence.from(
                    String.format("/services/game/%s/", serviceName).getBytes()
            );
            var response = etcdClient.getKVClient().get(prefix).get();

            List<EquivalentAddressGroup> addresses = response.getKvs().stream()
                    .map(kv -> new String(kv.getValue().getBytes()))
                    .map(addr -> {
                        String[] parts = addr.split(":");
                        return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
                    })
                    .map(SocketAddress.class::cast)
                    .map(EquivalentAddressGroup::new)
                    .collect(Collectors.toList());

            ResolutionResult result = ResolutionResult.newBuilder()
                    .setAddresses(addresses)
                    .build();

            listener.onResult(result);
        } catch (Exception e) {
            listener.onError(Status.UNAVAILABLE.withCause(e).withDescription("Failed to resolve from etcd"));
        }
        resolving = false;
    }


    /** 监听 etcd 变更，动态更新 */
    private void watchServiceChanges() {
        ByteSequence prefix = ByteSequence.from(
                String.format("/services/game/%s/", serviceName).getBytes()
        );
        etcdClient.getWatchClient().watch(prefix, watchResponse -> {
            boolean changed = watchResponse.getEvents().stream()
                    .anyMatch(e -> e.getEventType() == WatchEvent.EventType.PUT ||
                            e.getEventType() == WatchEvent.EventType.DELETE);
            if (changed) resolve();  // 有变更，重新拉取
        });
    }

    @Override
    public void shutdown() {
        // 关闭等清理工作
    }
}

