package com.card.game.gateway.service.impl;

import com.card.game.common.client.AgentGrpcClient;
import com.card.game.gateway.service.MessageHandler;
import com.card.game.gateway.util.MessageUtils;
import com.card.game.proto.agent.*;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MessageType;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Agent 消息处理器
 * 处理 AGENT_ 开头的消息类型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentMessageHandler implements MessageHandler {

    @Resource
    private AgentGrpcClient agentGrpcClient;

    @Override
    public String getMessageTypePrefix() {
        return "AGENT";
    }

    @Override
    public void handle(ChannelHandlerContext ctx, GameMessage message) {
        MessageType messageType = message.getMessageType();

        try {
            switch (messageType) {
                case AGENT_PLAYER_ONLINE_REQUEST:
                    handlePlayerOnline(ctx, message);
                    break;
                case AGENT_PLAYER_OFFLINE_REQUEST:
                    handlePlayerOffline(ctx, message);
                    break;
                case AGENT_FORWARD_MESSAGE_REQUEST:
                    handleForwardMessage(ctx, message);
                    break;
                case AGENT_GET_PLAYER_STATUS_REQUEST:
                    handleGetPlayerStatus(ctx, message);
                    break;
                case AGENT_BATCH_GET_PLAYER_STATUS_REQUEST:
                    handleBatchGetPlayerStatus(ctx, message);
                    break;
                case AGENT_BROADCAST_REQUEST:
                    handleBroadcast(ctx, message);
                    break;
                case AGENT_KICK_PLAYER_REQUEST:
                    handleKickPlayer(ctx, message);
                    break;
                default:
                    log.warn("不支持的 Agent 消息类型: {}", messageType);
                    MessageUtils.sendErrorMessage(ctx, message, "消息类型不支持");
            }
        } catch (Exception e) {
            log.error("处理 Agent 消息异常: messageType={}", messageType, e);
            MessageUtils.sendErrorMessage(ctx, message, e.getMessage());
        }
    }

    private void handlePlayerOnline(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        OnlineNotifyRequest request = OnlineNotifyRequest.parseFrom(message.getBody());
        log.info("收到玩家上线通知: playerId={}", request.getPlayerId());
        var response = agentGrpcClient.notifyPlayerOnline(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    private void handlePlayerOffline(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        OfflineNotifyRequest request = OfflineNotifyRequest.parseFrom(message.getBody());
        log.info("收到玩家下线通知: playerId={}, reason={}", request.getPlayerId(), request.getReason());
        var response = agentGrpcClient.notifyPlayerOffline(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    private void handleForwardMessage(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        ForwardMessageRequest request = ForwardMessageRequest.parseFrom(message.getBody());
        log.debug("收到消息转发请求: playerId={}, msgId={}", request.getPlayerId(), request.getMsgId());
        var response = agentGrpcClient.forwardMessage(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    private void handleGetPlayerStatus(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        GetPlayerStatusRequest request = GetPlayerStatusRequest.parseFrom(message.getBody());
        log.debug("收到查询玩家状态请求: playerId={}", request.getPlayerId());
        var response = agentGrpcClient.getPlayerStatus(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    private void handleBatchGetPlayerStatus(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        BatchGetPlayerStatusRequest request = BatchGetPlayerStatusRequest.parseFrom(message.getBody());
        log.debug("收到批量查询玩家状态请求: count={}", request.getPlayerIdsCount());
        var response = agentGrpcClient.batchGetPlayerStatus(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    private void handleBroadcast(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        BroadcastMessageRequest request = BroadcastMessageRequest.parseFrom(message.getBody());
        log.info("收到广播消息请求: msgId={}", request.getMsgId());
        var response = agentGrpcClient.broadcastMessage(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    private void handleKickPlayer(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        KickPlayerRequest request = KickPlayerRequest.parseFrom(message.getBody());
        log.info("收到踢下线请求: playerId={}", request.getPlayerId());
        boolean success = agentGrpcClient.kickPlayer(request);
        CommonResponse response = CommonResponse.newBuilder()
                .setCode(success ? Code.SUCCESS : Code.INTERNAL_ERROR)
                .setMessage(success ? "踢下线成功" : "踢下线失败")
                .build();
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }
}

