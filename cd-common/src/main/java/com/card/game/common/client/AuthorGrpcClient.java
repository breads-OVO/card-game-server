package com.card.game.common.client;

import com.card.game.proto.author.*;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorGrpcClient {

    private final LoginServiceGrpc.LoginServiceBlockingStub stub;

    public AuthorGrpcClient(Channel authorChannel) {
        this.stub = LoginServiceGrpc.newBlockingStub(authorChannel);
    }

    /**
     * 用户注册
     * @param request 注册请求消息
     * @return 注册响应
     */
    public RegisterResponse register(RegisterRequest request) {
        try {
            log.info("发起注册请求: username={}", request.getUsername());

            RegisterResponse response = stub.register(request);

            log.info("注册响应: username={}, code={}", request.getUsername(), response.getCode());
            return response;

        } catch (Exception e) {
            log.error("注册请求失败: username={}", request.getUsername(), e);
            throw new RuntimeException("注册服务调用失败", e);
        }
    }

    /**
     * 用户登录认证
     * @param request 登录请求消息
     * @return 登录响应
     */
    public AuthResponse authenticate(AuthRequest request) {
        try {
            log.info("发起登录请求: username={}, gatewayId={}",
                    request.getUsername(), request.getGatewayId());

            AuthResponse response = stub.authenticate(request);

            log.info("登录响应: username={}, code={}, playerId={}",
                    request.getUsername(), response.getCode(),
                    response.hasPlayerInfo() ? response.getPlayerInfo().getPlayerId() : null);
            return response;

        } catch (Exception e) {
            log.error("登录请求失败: username={}", request.getUsername(), e);
            throw new RuntimeException("登录服务调用失败", e);
        }
    }

    /**
     * 验证 Token
     * @param request Token验证请求消息
     * @return 验证响应
     */
    public VerifyTokenResponse verifyToken(VerifyTokenRequest request) {
        try {
            log.debug("发起Token验证请求: playerId={}", request.getPlayerId());

            VerifyTokenResponse response = stub.verifyToken(request);

            log.debug("Token验证响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response;

        } catch (Exception e) {
            log.error("Token验证失败: playerId={}", request.getPlayerId(), e);
            throw new RuntimeException("Token验证服务调用失败", e);
        }
    }

    /**
     * 刷新 Token
     * @param request Token刷新请求消息
     * @return 刷新响应
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            log.info("发起Token刷新请求");

            RefreshTokenResponse response = stub.refreshToken(request);

            log.info("Token刷新响应: code={}", response.getCode());
            return response;

        } catch (Exception e) {
            log.error("Token刷新失败", e);
            throw new RuntimeException("Token刷新服务调用失败", e);
        }
    }

    /**
     * 用户登出
     * @param request 登出请求消息
     * @return 是否成功
     */
    public boolean logout(LogoutRequest request) {
        try {
            log.info("发起登出请求: playerId={}, reason={}",
                    request.getPlayerId(), request.getReason());

            CommonResponse response = stub.logout(request);

            log.info("登出响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response.getCode() == Code.SUCCESS;

        } catch (Exception e) {
            log.error("登出请求失败: playerId={}", request.getPlayerId(), e);
            return false;
        }
    }

    /**
     * 踢玩家下线
     * @param request 踢下线请求消息
     * @return 是否成功
     */
    public boolean kickPlayer(KickPlayerRequest request) {
        try {
            log.info("发起踢下线请求: playerId={}, reason={}",
                    request.getPlayerId(), request.getKickReason());

            CommonResponse response = stub.kickPlayer(request);

            log.info("踢下线响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response.getCode() == Code.SUCCESS;

        } catch (Exception e) {
            log.error("踢下线请求失败: playerId={}", request.getPlayerId(), e);
            return false;
        }
    }
}
