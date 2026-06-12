package com.card.game.gateway.server.handler;

import com.card.game.common.client.AgentGrpcClient;
import com.card.game.gateway.service.MessageHandler;
import com.card.game.gateway.service.MessageHandlerFactory;
import com.card.game.gateway.session.ChannelSessionManager;
import com.card.game.proto.agent.OfflineNotifyRequest;
import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MessageType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * 服务器业务处理器
 * 处理解码后的 GameMessage
 *
 * 注意：Netty 的 ProtobufDecoder 已经将消息解码为 GameMessage 对象，
 * 所以这里直接接收 GameMessage，不需要再处理 DecodedMessage 包装类
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    @Resource
    private  ChannelSessionManager sessionManager;

    @Resource
    private  MessageHandlerFactory handlerFactory;

    @Resource
    private  AgentGrpcClient agentGrpcClient;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String clientAddr = getClientAddress(ctx);
        log.info("新连接建立: {}, 当前连接数: {}", clientAddr, sessionManager.getChannelCount());
        sessionManager.registerChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String clientAddr = getClientAddress(ctx);
        String playerId = sessionManager.getPlayerIdByChannel(ctx.channel());
        log.info("连接断开: {}, playerId={}, 剩余连接数: {}",
                clientAddr, playerId, sessionManager.getChannelCount() - 1);

        // 触发玩家下线处理
        if (playerId != null) {
            try {
                OfflineNotifyRequest notifyRequest = OfflineNotifyRequest.newBuilder()
                        .setPlayerId(playerId)
                        .setReason("disconnect")
                        .setOfflineTime(System.currentTimeMillis())
                        .build();
                agentGrpcClient.notifyPlayerOffline(notifyRequest);
            } catch (Exception e) {
                log.error("通知 Agent 玩家下线失败: playerId={}", playerId, e);
            }
            sessionManager.removeChannel(ctx.channel());
        } else {
            sessionManager.removeChannel(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 直接接收 Protobuf 解码后的 GameMessage
        if (msg instanceof GameMessage gameMessage) {
            String msgId = gameMessage.getHeader().getMsgId();

            log.debug("收到消息: msgId={}, seqId={}, channel={}",
                    msgId,
                    gameMessage.getHeader().getSeqId(),
                    ctx.channel().remoteAddress());

            // 根据消息类型分发处理
            handleMessage(ctx, gameMessage);
        } else {
            log.warn("未知消息类型: {}", msg.getClass().getName());
        }
    }

    /**
     * 消息分发处理 - 基于 MessageType 前缀自动路由
     */
    private void handleMessage(ChannelHandlerContext ctx, GameMessage gameMessage) {
        MessageType messageType = gameMessage.getMessageType();

        if (messageType == MessageType.UNKNOWN) {
            log.warn("消息类型为空或未知: msgId={}", gameMessage.getHeader().getMsgId());
            return;
        }

        MessageHandler handler = handlerFactory.getHandler(messageType);

        if (handler != null) {
            try {
                handler.handle(ctx, gameMessage);
            } catch (Exception e) {
                log.error("消息处理异常: messageType={}, error={}", messageType, e.getMessage(), e);
            }
        } else {
            log.warn("未找到消息类型的处理器: messageType={}, msgId={}",
                    messageType, gameMessage.getHeader().getMsgId());
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常: {}, 错误: {}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }

    private String getClientAddress(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            return address.getAddress().getHostAddress() + ":" + address.getPort();
        }
        return ctx.channel().remoteAddress().toString();
    }
}