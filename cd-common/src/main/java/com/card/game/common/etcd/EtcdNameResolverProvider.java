package com.card.game.common.etcd;

import io.etcd.jetcd.Client;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * etcd NameResolver 提供者
 */
@Component
public class EtcdNameResolverProvider extends NameResolverProvider {

    private static final String SCHEME = "discovery";

    private final Client etcdClient;

    public EtcdNameResolverProvider(Client etcdClient) {
        this.etcdClient = etcdClient;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5; // 优先级
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        String serviceName = targetUri.getHost();
        return new EtcdNameResolver(serviceName, etcdClient);
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }
}
