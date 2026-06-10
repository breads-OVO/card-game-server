package com.card.game.gateway.server;

import com.card.game.gateway.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * Netty 服务器
 * 负责启动和管理 Netty 服务端
 */
@Slf4j
@Component
public class GatewayNettyServer {

    @Resource
    private NettyConfig nettyConfig;

    @Resource
    private NettyServerInitializer initializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    /**
     * 启动 Netty 服务器
     */
    @PostConstruct
    public void start() {
        // 配置线程组
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreads());
        int workerThreads = nettyConfig.getWorkerThreads();
        workerGroup = workerThreads > 0
                ? new NioEventLoopGroup(workerThreads)
                : new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(initializer)
                    .option(ChannelOption.SO_BACKLOG, nettyConfig.getSoBacklog())
                    .childOption(ChannelOption.TCP_NODELAY, nettyConfig.isTcpNoDelay())
                    .childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.isSoKeepalive());

            // 绑定端口并启动
            channelFuture = bootstrap.bind(nettyConfig.getPort()).sync();

            log.info("Netty 服务器启动成功，监听端口: {}", nettyConfig.getPort());
            log.info("配置: boss线程数={}, worker线程数={}, 心跳间隔={}s, 空闲超时={}s",
                    nettyConfig.getBossThreads(),
                    workerThreads > 0 ? workerThreads : Runtime.getRuntime().availableProcessors() * 2,
                    nettyConfig.getHeartbeatInterval(),
                    nettyConfig.getIdleTimeout());

            // 添加关闭监听器
            channelFuture.channel().closeFuture().addListener(future -> log.info("Netty 服务器已关闭"));

        } catch (InterruptedException e) {
            log.error("Netty 服务器启动失败", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Netty 服务器启动失败", e);
            stop();
        }
    }

    /**
     * 优雅关闭 Netty 服务器
     */
    @PreDestroy
    public void stop() {
        log.info("正在关闭 Netty 服务器...");

        if (channelFuture != null) {
            channelFuture.channel().close().awaitUninterruptibly();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully().awaitUninterruptibly();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully().awaitUninterruptibly();
        }

        log.info("Netty 服务器已关闭");
    }

    /**
     * 获取服务器端口
     */
    public int getPort() {
        return nettyConfig.getPort();
    }

    /**
     * 检查服务器是否运行中
     */
    public boolean isRunning() {
        return channelFuture != null && channelFuture.channel().isActive();
    }
}