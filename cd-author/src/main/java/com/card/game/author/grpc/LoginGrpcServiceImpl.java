package com.card.game.author.grpc;

import com.card.game.author.dto.LoginResult;
import com.card.game.author.dto.RefreshResult;
import com.card.game.author.dto.RegisterResult;
import com.card.game.author.dto.VerifyResult;
import com.card.game.author.service.AuthService;
import com.card.game.proto.author.*;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.common.PlayerBaseInfo;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * LoginService gRPC 实现
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class LoginGrpcServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {

    private final AuthService authService;

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        log.info("收到注册请求: username={}", request.getUsername());

        RegisterResult result = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                "" // IP 可从 Context 获取，此处简化
        );

        RegisterResponse.Builder responseBuilder = RegisterResponse.newBuilder()
                .setCode(result.getCode())
                .setMessage(result.getMessage());

        if (result.getPlayerId() != null) {
            responseBuilder.setPlayerId(result.getPlayerId());
        }
        if (result.getCreateTime() != null) {
            responseBuilder.setCreateTime(result.getCreateTime());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();

        log.info("注册响应: code={}, playerId={}", result.getCode(), result.getPlayerId());
    }

    @Override
    public void authenticate(AuthRequest request, StreamObserver<AuthResponse> responseObserver) {
        log.info("收到登录请求: username={}, gatewayId={}, clientType={}",
                request.getUsername(), request.getGatewayId(), request.getClientType());

        LoginResult result = authService.authenticate(
                request.getUsername(),
                request.getPassword(),
                request.getGatewayId(),
                request.getClientType(),
                request.getClientVersion(),
                request.getDeviceId()
        );

        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder()
                .setCode(result.getCode())
                .setMessage(result.getMessage())
                .setServerTime(System.currentTimeMillis());

        if (result.getToken() != null) {
            responseBuilder.setToken(result.getToken());
        }
        if (result.getExpireAt() != null) {
            responseBuilder.setExpireAt(result.getExpireAt());
        }
        if (result.getAccount() != null) {
            responseBuilder.setPlayerInfo(PlayerBaseInfo.newBuilder()
                    .setPlayerId(result.getAccount().getId())
                    .setUsername(result.getAccount().getUsername())
                    .setNickname(result.getAccount().getNickname())
                    .setLevel(1)
                    .setAvatar(result.getAccount().getAvatar() != null ? result.getAccount().getAvatar() : "")
                    .build());
        }
        responseBuilder.setIsReconnect(result.isReconnect());

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();

        log.info("登录响应: code={}, playerId={}", result.getCode(),
                result.getAccount() != null ? result.getAccount().getId() : null);
    }

    @Override
    public void verifyToken(VerifyTokenRequest request, StreamObserver<VerifyTokenResponse> responseObserver) {
        log.debug("收到Token校验请求: token={}, playerId={}", maskToken(request.getToken()), request.getPlayerId());

        VerifyResult result = authService.verifyToken(
                request.getToken(),
                 request.getPlayerId()
        );

        VerifyTokenResponse.Builder responseBuilder = VerifyTokenResponse.newBuilder()
                .setCode(result.getCode())
                .setMessage(result.getMessage());

        if (result.getPlayerId() != null) {
            responseBuilder.setPlayerId(result.getPlayerId());
        }
        if (result.getUsername() != null) {
            responseBuilder.setUsername(result.getUsername());
        }
        if (result.getExpireAt() != null) {
            responseBuilder.setExpireAt(result.getExpireAt());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<RefreshTokenResponse> responseObserver) {
        log.info("收到刷新Token请求");

        RefreshResult result = authService.refreshToken(request.getToken());

        RefreshTokenResponse.Builder responseBuilder = RefreshTokenResponse.newBuilder()
                .setCode(result.getCode())
                .setMessage(result.getMessage());

        if (result.getNewToken() != null) {
            responseBuilder.setNewToken(result.getNewToken());
        }
        if (result.getExpireAt() != null) {
            responseBuilder.setExpireAt(result.getExpireAt());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();

        log.info("刷新Token响应: code={}", result.getCode());
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        log.info("收到登出请求: playerId={}, reason={}", request.getPlayerId(), request.getReason());

        boolean success = authService.logout(request.getPlayerId(), request.getToken(), request.getReason());


        LogoutResponse response = LogoutResponse.newBuilder()
                .setCode(success ? Code.SUCCESS : Code.INTERNAL_ERROR)
                .setMessage(success ? "登出成功" : "登出失败")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void kickPlayer(KickPlayerRequest request, StreamObserver<CommonResponse> responseObserver) {
        log.info("收到踢下线请求: playerId={}, reason={}", request.getPlayerId(), request.getKickReason());

        boolean success = authService.kickPlayer(request.getPlayerId(), request.getKickReason(), request.getKickCode());

        CommonResponse response = CommonResponse.newBuilder()
                .setCode(success ? Code.SUCCESS : Code.INTERNAL_ERROR)
                .setMessage(success ? "踢下线成功" : "踢下线失败")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 隐藏 Token（用于日志）
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
