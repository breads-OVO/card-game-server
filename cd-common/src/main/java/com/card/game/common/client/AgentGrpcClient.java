package com.card.game.common.client;

import com.card.game.common.etcd.GrpcChannelFactory;
import com.card.game.proto.agent.*;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentGrpcClient {

    private final GrpcChannelFactory channelFactory;
    private final String serviceName;
    private volatile AgentServiceGrpc.AgentServiceBlockingStub stub;

    public AgentGrpcClient(GrpcChannelFactory channelFactory, String serviceName) {
        this.channelFactory = channelFactory;
        this.serviceName = serviceName;
    }

    private AgentServiceGrpc.AgentServiceBlockingStub getStub() {
        if (stub == null) {
            synchronized (this) {
                if (stub == null) {
                    stub = AgentServiceGrpc.newBlockingStub(channelFactory.getChannel(serviceName));
                }
            }
        }
        return stub;
    }

    /**
     * 玩家上线通知
     */
    public CommonResponse notifyPlayerOnline(OnlineNotifyRequest request) {
        try {
            log.info("发送玩家上线通知: playerId={}", request.getPlayerId());
            CommonResponse response = getStub().notifyPlayerOnline(request);
            log.info("玩家上线通知响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response;
        } catch (Exception e) {
            log.error("玩家上线通知失败: playerId={}", request.getPlayerId(), e);
            throw new RuntimeException("玩家上线通知服务调用失败", e);
        }
    }

    /**
     * 玩家下线通知
     */
    public CommonResponse notifyPlayerOffline(OfflineNotifyRequest request) {
        try {
            log.info("发送玩家下线通知: playerId={}, reason={}", request.getPlayerId(), request.getReason());
            CommonResponse response = getStub().notifyPlayerOffline(request);
            log.info("玩家下线通知响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response;
        } catch (Exception e) {
            log.error("玩家下线通知失败: playerId={}", request.getPlayerId(), e);
            throw new RuntimeException("玩家下线通知服务调用失败", e);
        }
    }

    /**
     * 转发业务消息
     */
    public ForwardMessageResponse forwardMessage(ForwardMessageRequest request) {
        try {
            log.debug("发送消息转发请求: playerId={}, msgId={}", request.getPlayerId(), request.getMsgId());
            ForwardMessageResponse response = getStub().forwardMessage(request);
            log.debug("消息转发响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response;
        } catch (Exception e) {
            log.error("消息转发失败: playerId={}", request.getPlayerId(), e);
            throw new RuntimeException("消息转发服务调用失败", e);
        }
    }

    /**
     * 查询玩家状态
     */
    public GetPlayerStatusResponse getPlayerStatus(GetPlayerStatusRequest request) {
        try {
            log.debug("查询玩家状态: playerId={}", request.getPlayerId());
            GetPlayerStatusResponse response = getStub().getPlayerStatus(request);
            log.debug("玩家状态响应: playerId={}, online={}", request.getPlayerId(), response.getOnline());
            return response;
        } catch (Exception e) {
            log.error("查询玩家状态失败: playerId={}", request.getPlayerId(), e);
            throw new RuntimeException("玩家状态查询服务调用失败", e);
        }
    }

    /**
     * 批量查询玩家状态
     */
    public BatchGetPlayerStatusResponse batchGetPlayerStatus(BatchGetPlayerStatusRequest request) {
        try {
            log.debug("批量查询玩家状态: count={}", request.getPlayerIdsCount());
            BatchGetPlayerStatusResponse response = getStub().batchGetPlayerStatus(request);
            log.debug("批量查询玩家状态响应: count={}", response.getPlayersCount());
            return response;
        } catch (Exception e) {
            log.error("批量查询玩家状态失败", e);
            throw new RuntimeException("批量查询玩家状态服务调用失败", e);
        }
    }

    /**
     * 广播消息给所有在线玩家
     */
    public CommonResponse broadcastMessage(BroadcastMessageRequest request) {
        try {
            log.info("发送广播消息: msgId={}", request.getMsgId());
            CommonResponse response = getStub().broadcastMessage(request);
            log.info("广播消息响应: code={}", response.getCode());
            return response;
        } catch (Exception e) {
            log.error("广播消息失败: msgId={}", request.getMsgId(), e);
            throw new RuntimeException("广播消息服务调用失败", e);
        }
    }

    /**
     * 踢玩家下线
     */
    public boolean kickPlayer(KickPlayerRequest request) {
        try {
            log.info("发起踢下线请求: playerId={}, reason={}", request.getPlayerId(), request.getReason());
            CommonResponse response = getStub().kickPlayer(request);
            log.info("踢下线响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response.getCode() == Code.SUCCESS;
        } catch (Exception e) {
            log.error("踢下线请求失败: playerId={}", request.getPlayerId(), e);
            return false;
        }
    }
}
