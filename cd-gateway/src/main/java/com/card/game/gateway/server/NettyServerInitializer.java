package com.card.game.gateway.server;

import com.card.game.gateway.server.handler.HeartbeatHandler;
import com.card.game.gateway.server.handler.ServerChannelHandler;
import com.card.game.proto.common.GameMessage;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import com.card.game.gateway.config.NettyConfig;

import javax.annotation.Resource;

/**
 * Netty Channel 初始化器
 *
 * Pipeline 顺序（从入站到出站）：
 *
 * 入站方向（客户端 -> 服务器）：
 * 1. IdleStateHandler          - 空闲状态检测
 * 2. ProtobufVarint32FrameDecoder - 帧解码（处理半包/粘包，读取 varint32 长度前缀）
 * 3. ProtobufDecoder           - Protobuf 解码（bytes -> GameMessage）
 * 4. HeartbeatHandler          - 心跳处理
 * 5. ServerChannelHandler       - 业务处理
 *
 * 出站方向（服务器 -> 客户端）：
 * 5. ServerChannelHandler       - 业务处理（返回响应）
 * 4. HeartbeatHandler
 * 3. ProtobufEncoder            - Protobuf 编码（GameMessage -> bytes）
 * 2. ProtobufVarint32LengthFieldPrepender - 添加 varint32 长度前缀
 * 1. IdleStateHandler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    private  NettyConfig nettyConfig;

    @Resource
    private  ServerChannelHandler serverChannelHandler;

    @Resource
    private  HeartbeatHandler heartbeatHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // ========== 入站处理器（按顺序） ==========

        // 1. 空闲状态检测（读空闲超时关闭连接）
        /*读空闲超时：客户端在指定秒数内无数据发送，触发超时事件（用于踢掉僵尸连接）
        写/读写空闲不检测：设为 0 表示不监控服务端发送和双向空闲状态
        用途：自动清理长时间不活动的客户端连接，释放服务器资源*/
        pipeline.addLast("idleStateHandler", new IdleStateHandler(
                nettyConfig.getIdleTimeout(),  // 读空闲超时（秒）
                0,                             // 写空闲超时不检测
                0,                             // 读写空闲超时不检测
                TimeUnit.SECONDS
        ));

        // 2. 帧解码器：处理半包/粘包，读取 varint32 长度前缀，截取完整帧
        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());

        // 3. Protobuf 解码器：将字节数组解码为 GameMessage 对象
        pipeline.addLast("protobufDecoder", new ProtobufDecoder(GameMessage.getDefaultInstance()));

        // ========== 出站处理器（按顺序） ==========

        // 4. 长度字段前缀编码器：在消息前添加 varint32 长度前缀
        pipeline.addLast("lengthFieldPrepender", new ProtobufVarint32LengthFieldPrepender());

        // 5. Protobuf 编码器：将 GameMessage 对象编码为字节数组
        pipeline.addLast("protobufEncoder", new ProtobufEncoder());

        // ========== 业务处理器 ==========

        // 6. 心跳处理器
        pipeline.addLast("heartbeatHandler", heartbeatHandler);

        // 7. 业务处理器
        pipeline.addLast("serverChannelHandler", serverChannelHandler);

        log.debug("Channel 初始化完成: {}", ch.remoteAddress());
    }
}