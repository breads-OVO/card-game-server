package com.card.game.gateway.service.impl;
import com.card.game.gateway.service.MessageHandler;
import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MessageType;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Agent 消息处理器
 * 处理 AGENT_ 开头的消息类型
 */
@Slf4j
@Component
public class AgentMessageHandler implements MessageHandler {

    // TODO: 注入 Agent gRPC 客户端

    @Override
    public String getMessageTypePrefix() {
        return "AGENT";
    }

    @Override
    public void handle(ChannelHandlerContext ctx, GameMessage message) {
        MessageType messageType = message.getMessageType();

        log.debug("收到 Agent 消息: {}", messageType);

        try {
            switch (messageType) {
                case AGENT_PLAYER_ONLINE_REQUEST:
                    handlePlayerOnline(ctx, message);
                    break;
                case AGENT_FORWARD_MESSAGE_REQUEST:
                    handleForwardMessage(ctx, message);
                    break;
                case AGENT_BROADCAST_REQUEST:
                    handleBroadcast(ctx, message);
                    break;
                default:
                    log.warn("不支持的 Agent 消息类型: {}", messageType);
            }
        } catch (Exception e) {
            log.error("处理 Agent 消息异常: messageType={}", messageType, e);
        }
    }

    private void handlePlayerOnline(ChannelHandlerContext ctx, GameMessage message) {
        log.info("处理玩家上线通知");
        // TODO: 调用 Agent gRPC 服务
    }

    private void handleForwardMessage(ChannelHandlerContext ctx, GameMessage message) {
        log.debug("转发业务消息");
        // TODO: 转发到 Agent 服务
    }

    private void handleBroadcast(ChannelHandlerContext ctx, GameMessage message) {
        log.info("处理广播消息");
        // TODO: 实现广播逻辑
    }
}

