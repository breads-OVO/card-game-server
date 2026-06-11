package com.card.game.gateway.util;

import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MsgHeader;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;

public class MessageUtils {

    public static void sendMessage(ChannelHandlerContext ctx, GameMessage message, ByteString body) {
        MsgHeader header = message.getHeader();
        GameMessage response  = GameMessage.newBuilder()
                .setHeader(MsgHeader.newBuilder()
                        .setMsgId(header.getMsgId())
                        .setSeqId(header.getSeqId())
                        .setTimestamp(System.currentTimeMillis())
                        .build())
                .setBody(body)
                .setMessageType(message.getMessageType())
                .build();
        ctx.writeAndFlush(response);
    }
}
