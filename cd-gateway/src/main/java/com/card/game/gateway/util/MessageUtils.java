package com.card.game.gateway.util;

import com.card.game.common.util.IdGenerator;
import com.card.game.proto.common.*;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;

public class MessageUtils {

    public static void sendMessage(ChannelHandlerContext ctx, GameMessage message, ByteString body) {
        MsgHeader header = message.getHeader();
        GameMessage response  = GameMessage.newBuilder()
                .setHeader(MsgHeader.newBuilder()
                        .setMsgId(IdGenerator.getUUID())
                        .setSeqId(header.getSeqId())
                        .setTimestamp(System.currentTimeMillis())
                        .build())
                .setBody(body)
                .setMessageType(MessageType.RESPONSE_CLIENT)
                .build();
        ctx.writeAndFlush(response);
    }

    public static void sendErrorMessage(ChannelHandlerContext ctx, GameMessage message, String messageText) {
        CommonResponse errorResponse = CommonResponse.newBuilder()
                .setCode(Code.INTERNAL_ERROR)
                .setMessage(messageText)
                .build();
        ByteString body = errorResponse.toByteString();
        MsgHeader header = message.getHeader();
        GameMessage response  = GameMessage.newBuilder()
                .setHeader(MsgHeader.newBuilder()
                        .setMsgId(IdGenerator.getUUID())
                        .setSeqId(header.getSeqId())
                        .setTimestamp(System.currentTimeMillis())
                        .build())
                .setBody(body)
                .setMessageType(MessageType.RESPONSE_CLIENT)
                .build();
        ctx.writeAndFlush(response);
    }
}
