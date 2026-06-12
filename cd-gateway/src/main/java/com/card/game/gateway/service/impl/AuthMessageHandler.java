package com.card.game.gateway.service.impl;

import com.card.game.common.client.AuthorGrpcClient;
import com.card.game.gateway.service.MessageHandler;
import com.card.game.gateway.util.MessageUtils;
import com.card.game.proto.author.*;
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
 * 认证消息处理器
 * 处理 AUTH_ 开头的消息类型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMessageHandler implements MessageHandler {

    @Resource
    private AuthorGrpcClient authorGrpcClient;

    @Override
    public String getMessageTypePrefix() {
        return "AUTH";
    }

    @Override
    public void handle(ChannelHandlerContext ctx, GameMessage message) {
        MessageType messageType = message.getMessageType();

        try {
            switch (messageType) {
                case AUTH_LOGIN_REQUEST:
                    handleLogin(ctx, message);
                    break;
                case AUTH_REGISTER_REQUEST:
                    handleRegister(ctx, message);
                    break;
                case AUTH_LOGOUT_REQUEST:
                    handleLogout(ctx, message);
                    break;
                default:
                    log.warn("不支持的认证消息类型: {}", messageType);
                    MessageUtils.sendErrorMessage(ctx, message, "消息类型不支持");
            }
        } catch (Exception e) {
            log.error("处理认证消息异常: messageType={}", messageType, e);
            MessageUtils.sendErrorMessage(ctx, message, e.getMessage());
        }
    }

    /**
     * 处理登录请求
     */
    private void handleLogin(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        // 反序列化 body 为 AuthRequest
        AuthRequest request = AuthRequest.parseFrom(message.getBody());
        log.info("收到登录请求: username={}", request.getUsername());
        AuthResponse response = authorGrpcClient.authenticate(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    /**
     * 处理注册请求
     */
    private void handleRegister(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        RegisterRequest request = RegisterRequest.parseFrom(message.getBody());
        log.info("收到注册请求: username:{}", request.getUsername());
        RegisterResponse response = authorGrpcClient.register(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

    /**
     * 处理登出请求
     */
    private void handleLogout(ChannelHandlerContext ctx, GameMessage message) throws InvalidProtocolBufferException {
        LogoutRequest request = LogoutRequest.parseFrom(message.getBody());
        log.info("收到登出请求: playerID:{}", request.getPlayerId());
        LogoutResponse response = authorGrpcClient.logout(request);
        ByteString body = response.toByteString();
        MessageUtils.sendMessage(ctx, message, body);
    }

}

