package com.card.game.gateway.service.impl;

import com.card.game.gateway.service.MessageHandler;
import com.card.game.gateway.util.MessageUtils;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.common.GameMessage;
import com.google.protobuf.ByteString;
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
        CommonResponse response = CommonResponse.newBuilder()
                .setCode(Code.SUCCESS)
                .setMessage(String.valueOf(System.currentTimeMillis()))
                .build();
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    @Override
    public String getMessageTypePrefix() {
        return "HEART";
    }
}
