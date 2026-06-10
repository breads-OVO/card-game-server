package com.card.game.gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Netty 配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "netty")
public class NettyConfig {

    /** 监听端口 */
    private int port ;

    /** Boss 线程数 */
    private int bossThreads = 1;

    /** Worker 线程数（0 表示 CPU 核心数 × 2） */
    private int workerThreads = 0;

    /** 心跳间隔（秒） */
    private int heartbeatInterval = 30;

    /** 空闲超时（秒） */
    private int idleTimeout = 90;

    /** SO_BACKLOG（等待队列大小） */
    private int soBacklog = 1024;

    /** TCP_NODELAY（禁用 Nagle 算法） */
    private boolean tcpNoDelay = true;

    /** SO_KEEPALIVE */
    private boolean soKeepalive = true;
}