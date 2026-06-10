package com.card.game.agent.grpc;

import com.card.game.agent.dto.PlayerSession;
import com.card.game.agent.service.MessageForwardService;
import com.card.game.agent.service.PlayerSessionManagerService;
import com.card.game.proto.agent.*;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * AgentService gRPC 实现
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AgentGrpcServiceImpl extends AgentServiceGrpc.AgentServiceImplBase {

    @Resource
    private  PlayerSessionManagerService sessionManager;

    @Resource
    private  MessageForwardService messageForwardService;

    @Override
    public void notifyPlayerOnline(OnlineNotifyRequest request,
                                   StreamObserver<CommonResponse> responseObserver) {
        log.info("收到玩家上线通知: playerId={}, gatewayId={}, sessionId={}",
                request.getPlayerId(), request.getGatewayId(), request.getSessionId());

        // 构建玩家会话
        PlayerSession session = sessionManager.toPlayerSession(
                request.getPlayerInfo(),
                request.getGatewayId(),
                request.getSessionId(),
                request.getToken()
        );

        if (session == null) {
            log.warn("玩家上线通知失败: playerInfo 为空");
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCode(Code.INVALID_PARAM)
                    .setMessage("玩家信息无效")
                    .setTimestamp(System.currentTimeMillis())
                    .build());
            responseObserver.onCompleted();
            return;
        }

        // 设置登录时间
        session.setLoginTime(request.getLoginTime() > 0 ? request.getLoginTime() : System.currentTimeMillis());

        // 玩家上线
        boolean success = sessionManager.playerOnline(session);

        CommonResponse response = CommonResponse.newBuilder()
                .setCode(success ? Code.SUCCESS : Code.INTERNAL_ERROR)
                .setMessage(success ? "玩家上线成功" : "玩家上线失败")
                .setTimestamp(System.currentTimeMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        log.info("玩家上线通知完成: playerId={}, success={}", request.getPlayerId(), success);
    }

    @Override
    public void notifyPlayerOffline(OfflineNotifyRequest request,
                                    StreamObserver<CommonResponse> responseObserver) {
        log.info("收到玩家下线通知: playerId={}, reason={}", request.getPlayerId(), request.getReason());

        boolean success = sessionManager.playerOffline(request.getPlayerId(), request.getReason());

        CommonResponse response = CommonResponse.newBuilder()
                .setCode(success ? Code.SUCCESS : Code.INTERNAL_ERROR)
                .setMessage(success ? "玩家下线成功" : "玩家下线失败")
                .setTimestamp(System.currentTimeMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void forwardMessage(ForwardMessageRequest request,
                               StreamObserver<ForwardMessageResponse> responseObserver) {
        log.debug("收到消息转发请求: playerId={}, msgId={}, seqId={}",
                request.getPlayerId(), request.getMsgId(), request.getSeqId());

        ForwardMessageResponse response = messageForwardService.processMessage(
                request.getPlayerId(),
                request.getMsgId(),
                request.getBody().toByteArray(),
                request.getTimestamp(),
                request.getSeqId()
        );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPlayerStatus(GetPlayerStatusRequest request,
                                StreamObserver<GetPlayerStatusResponse> responseObserver) {
        log.debug("查询玩家状态: playerId={}", request.getPlayerId());

        PlayerSession session = sessionManager.getPlayerSession(request.getPlayerId());

        GetPlayerStatusResponse.Builder responseBuilder = GetPlayerStatusResponse.newBuilder();

        if (session != null) {
            responseBuilder.setCode(Code.SUCCESS)
                    .setMessage("查询成功")
                    .setOnline(true)
                    .setSession(sessionManager.toGrpcSession(session));
        } else {
            responseBuilder.setCode(Code.SUCCESS)
                    .setMessage("玩家不在线")
                    .setOnline(false)
                    .setOfflineTime(System.currentTimeMillis());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void batchGetPlayerStatus(BatchGetPlayerStatusRequest request,
                                     StreamObserver<BatchGetPlayerStatusResponse> responseObserver) {
        log.debug("批量查询玩家状态: playerIds={}", request.getPlayerIdsList());

        List<String> playerIds = request.getPlayerIdsList();
        Map<String, PlayerSession> sessions = sessionManager.batchGetPlayerSessions(playerIds);

        Map<String, PlayerStatusItem> resultMap = new HashMap<>();

        for (String playerId : playerIds) {
            PlayerSession session = sessions.get(playerId);
            PlayerStatusItem.Builder itemBuilder = PlayerStatusItem.newBuilder();

            if (session != null) {
                itemBuilder.setOnline(true)
                        .setSession(sessionManager.toGrpcSession(session));
            } else {
                itemBuilder.setOnline(false)
                        .setOfflineTime(System.currentTimeMillis());
            }
            resultMap.put(playerId, itemBuilder.build());
        }

        BatchGetPlayerStatusResponse response = BatchGetPlayerStatusResponse.newBuilder()
                .setCode(Code.SUCCESS)
                .setMessage("查询成功")
                .putAllPlayers(resultMap)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void broadcastMessage(BroadcastMessageRequest request,
                                 StreamObserver<CommonResponse> responseObserver) {
        log.info("收到广播消息请求: msgId={}, targetStatus={}, excludeCount={}",
                request.getMsgId(), request.getTargetStatus(), request.getExcludePlayerIdsCount());

        // 获取所有在线玩家
        var allPlayers = sessionManager.getAllOnlinePlayerIds();

        // 排除指定玩家
        var excludeSet = new HashSet<>(request.getExcludePlayerIdsList());

        long targetCount = allPlayers.stream()
                .filter(id -> !excludeSet.contains(id))
                .count();

        // TODO: V2 版本实现真正的广播
        // 需要遍历所有在线玩家，将消息发送到对应的 Gateway

        log.info("广播消息: 目标玩家数={}", targetCount);

        CommonResponse response = CommonResponse.newBuilder()
                .setCode(Code.SUCCESS)
                .setMessage(String.format("广播消息已发送，目标玩家数=%d", targetCount))
                .setTimestamp(System.currentTimeMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void kickPlayer(KickPlayerRequest request,
                           StreamObserver<CommonResponse> responseObserver) {
        log.info("收到踢下线请求: playerId={}, reason={}", request.getPlayerId(), request.getReason());

        boolean success = sessionManager.kickPlayer(request.getPlayerId(), request.getReason());

        CommonResponse response = CommonResponse.newBuilder()
                .setCode(success ? Code.SUCCESS : Code.INTERNAL_ERROR)
                .setMessage(success ? "踢下线成功" : "踢下线失败")
                .setTimestamp(System.currentTimeMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
