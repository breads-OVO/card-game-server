package com.card.game.gateway.service;

import com.card.game.proto.common.GameMessage;
import io.netty.channel.ChannelHandlerContext;

public interface MessageHandler {

    /**
     * 处理消息
     * @param ctx Netty 通道上下文
     * @param message 游戏消息
     */
    void handle(ChannelHandlerContext ctx, GameMessage message);

    /**
     * 获取支持的消息类型前缀
     * 例如: "AUTH"、"AGENT"、"GATEWAY"
     * @return 消息类型前缀
     */
    String getMessageTypePrefix();
}
