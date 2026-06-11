package com.card.game.gateway.service.impl;

import com.card.game.gateway.service.MessageHandler;
import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MsgHeader;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 心跳消息处理器
 * 处理 Heart_ 开头的消息类型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeartBeatMessageHandler implements MessageHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMessage message) {
//        log.debug("收到心跳消息");
        MsgHeader header = message.getHeader();
//        log.debug("心跳消息头: {}", header);
    }

    @Override
    public String getMessageTypePrefix() {
        return "HEART";
    }
}
