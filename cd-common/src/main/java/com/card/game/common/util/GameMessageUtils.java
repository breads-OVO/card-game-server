package com.card.game.common.util;

import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MessageType;
import com.card.game.proto.common.MsgHeader;
import com.google.protobuf.ByteString;

public class GameMessageUtils {

    /**
     * 构建简单消息
     */
    public static GameMessage buildMessage(MessageType type, ByteString body) {
        MsgHeader header = MsgHeader.newBuilder()
                .setMsgId(IdGenerator.getUUID())
                .setTimestamp(System.currentTimeMillis())
                .build();
        return GameMessage.newBuilder()
                .setHeader(header)
                .setMessageType(type)
                .setBody(body)
                .build();

    }
}
