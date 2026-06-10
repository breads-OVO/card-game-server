package com.card.game.common.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * etcd 服务注册器
 * 自动将当前服务注册到 etcd
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtcdServiceRegistry {

    private final Client etcdClient;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${grpc.server.port}")
    private int serverPort;

    @Value("${server.address:auto}")
    private String serverAddress;

    @Value("${grpc.server.keep-alive-time:30}")
    private long LEASE_TTL = 30; // 租约过期时间（秒）

    @Value("${grpc.server.keep-alive-timeout:5}")
    private long OPERATION_TIMEOUT = 5; // 操作超时时间（秒）
    private long leaseId;


    /**
     * 自动获取本机 IP 地址
     * 优先使用配置的地址，如果为 auto 则自动检测
     */
    private String getServerAddress() {
        if (!"auto".equalsIgnoreCase(serverAddress)) {
            return serverAddress;
        }

        try {
            // 方法1：获取第一个非回环、非链路本地的 IPv4 地址
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // 跳过未启用、回环接口
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    // 跳过回环地址、链路本地地址、多播地址
                    if (address.isLoopbackAddress() ||
                            address.isLinkLocalAddress() ||
                            address.isMulticastAddress()) {
                        continue;
                    }

                    // 返回第一个有效的 IPv4 地址
                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();
                        log.info("自动检测到服务器 IP: {} (网卡: {})", ip, networkInterface.getDisplayName());
                        return ip;
                    }
                }
            }

            // 方法2：如果没找到，使用 localhost
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            log.warn("未能自动检测 IP，使用默认地址: {}", hostAddress);
            return hostAddress;

        } catch (Exception e) {
            log.error("获取服务器 IP 失败，使用 127.0.0.1", e);
            return "127.0.0.1";
        }
    }

    @PostConstruct
    public void register() {
        try {
            log.info("正在注册服务到 etcd: {} -> {}:{}", serviceName, serverAddress, serverPort);

            // 创建租约（带超时）
            var leaseGrant = etcdClient.getLeaseClient()
                    .grant(LEASE_TTL)
                    .get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
            leaseId = leaseGrant.getID();

            // 服务地址
            String serviceKey = String.format("/services/game/%s/%s:%d",
                    serviceName, serverAddress, serverPort);
            String serviceValue = String.format("%s:%d", serverAddress, serverPort);

            // 注册服务（带租约和超时）
            etcdClient.getKVClient().put(
                    ByteSequence.from(serviceKey, StandardCharsets.UTF_8),
                    ByteSequence.from(serviceValue, StandardCharsets.UTF_8),
                    io.etcd.jetcd.options.PutOption.newBuilder()
                            .withLeaseId(leaseId)
                            .build()
            ).get(OPERATION_TIMEOUT, TimeUnit.SECONDS);

            log.info("✅ 服务注册成功: {} -> {}:{}", serviceKey, serverAddress, serverPort);

            // 启动心跳续租
            startKeepAlive();

        } catch (Exception e) {
            log.warn("⚠️ etcd 服务注册失败（应用仍将继续运行）: {}", e.getMessage());
            log.debug("etcd 注册详细错误:", e);
        }
    }

    /**
     * 心跳续租
     */
    private void startKeepAlive() {
        Thread keepAliveThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        etcdClient.getLeaseClient()
                                .keepAliveOnce(leaseId)
                                .get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
                        log.debug("租约续期成功: leaseId={}", leaseId);
                    } catch (Exception e) {
                        log.debug("租约续期失败: {}", e.getMessage());
                    }
                    TimeUnit.SECONDS.sleep(LEASE_TTL / 2);
                }
            } catch (InterruptedException e) {
                log.debug("心跳线程被中断");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("心跳异常", e);
            }
        }, "etcd-keepalive");

        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    @PreDestroy
    public void deregister() {
        try {
            String serviceKey = String.format("/services/game/%s/%s:%d",
                    serviceName, serverAddress, serverPort);

            etcdClient.getKVClient().delete(
                    ByteSequence.from(serviceKey, StandardCharsets.UTF_8)
            ).get(OPERATION_TIMEOUT, TimeUnit.SECONDS);

            etcdClient.getLeaseClient().revoke(leaseId)
                    .get(OPERATION_TIMEOUT, TimeUnit.SECONDS);

            log.info("服务注销成功: {}", serviceKey);
        } catch (Exception e) {
            log.warn("服务注销失败: {}", e.getMessage());
        }
    }
}
