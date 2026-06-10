package com.card.game.gateway.service.impl;

import com.card.game.common.util.IdGenerator;
import com.card.game.gateway.service.MessageHandler;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.GameMessage;
import com.card.game.proto.common.MessageType;
import com.card.game.proto.author.AuthRequest;
import com.card.game.proto.author.AuthResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 认证消息处理器
 * 处理 AUTH_ 开头的消息类型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMessageHandler implements MessageHandler {

    // TODO: 注入 Login gRPC 客户端
    // private final LoginServiceGrpc.LoginServiceBlockingStub loginServiceStub;

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
                    sendErrorResponse(ctx, message, Code.INVALID_PARAM, "不支持的消息类型");
            }
        } catch (Exception e) {
            log.error("处理认证消息异常: messageType={}", messageType, e);
            sendErrorResponse(ctx, message, Code.INTERNAL_ERROR, "服务器内部错误");
        }
    }

    /**
     * 处理登录请求
     */
    private void handleLogin(ChannelHandlerContext ctx, GameMessage message) {
        try {
            // 反序列化 body 为 AuthRequest
            AuthRequest request = AuthRequest.parseFrom(message.getBody());

            log.info("收到登录请求: username={}", request.getUsername());

            // TODO: 调用 Login gRPC 服务
            // AuthResponse response = loginServiceStub.authenticate(request);

            // 临时返回模拟响应
            AuthResponse mockResponse = AuthResponse.newBuilder()
                    .setCode(Code.SUCCESS)
                    .setMessage("登录成功")
                    .setToken("mock_token")
                    .setExpireAt(System.currentTimeMillis() + 3600000)
                    .build();

            // 构建响应消息
            GameMessage response = GameMessage.newBuilder()
                    .setHeader(message.getHeader().toBuilder()
                            .setMsgId(IdGenerator.getUUID())
                            .setTimestamp(System.currentTimeMillis())
                            .build())
                    .setBody(mockResponse.toByteString())
                    .setMessageType(MessageType.AUTH_LOGIN_RESPONSE)
                    .build();

            ctx.writeAndFlush(response);

        } catch (InvalidProtocolBufferException e) {
            log.error("解析登录请求失败", e);
            sendErrorResponse(ctx, message, Code.INVALID_PARAM, "请求格式错误");
        }
    }

    /**
     * 处理注册请求
     */
    private void handleRegister(ChannelHandlerContext ctx, GameMessage message) {
        log.info("收到注册请求");
        // TODO: 实现注册逻辑
        sendErrorResponse(ctx, message, Code.SERVER_BUSY, "功能开发中");
    }

    /**
     * 处理登出请求
     */
    private void handleLogout(ChannelHandlerContext ctx, GameMessage message) {
        log.info("收到登出请求");
        // TODO: 实现登出逻辑
        sendErrorResponse(ctx, message, Code.SUCCESS, "登出成功");
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, GameMessage request,
                                   Code code, String message) {
        GameMessage response = GameMessage.newBuilder()
                .setHeader(request.getHeader().toBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .build())
                .setMessageType(MessageType.UNKNOWN)
                .build();

        ctx.writeAndFlush(response);
    }
}

