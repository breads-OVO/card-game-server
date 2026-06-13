package com.card.game.common.client;

import com.card.game.common.etcd.GrpcChannelFactory;
import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.gateway.GatewayServiceGrpc;
import com.card.game.proto.gateway.PushToAllRequest;
import com.card.game.proto.gateway.PushToPlayerGroupRequest;
import com.card.game.proto.gateway.PushToPlayerRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GatewayGrpcClient {

    private final GrpcChannelFactory channelFactory;
    private final String serviceName;
    private volatile GatewayServiceGrpc.GatewayServiceBlockingStub stub;

    public GatewayGrpcClient(GrpcChannelFactory channelFactory, String serviceName) {
        this.channelFactory = channelFactory;
        this.serviceName = serviceName;
    }

    private GatewayServiceGrpc.GatewayServiceBlockingStub getStub() {
        if (stub == null) {
            synchronized (this) {
                if (stub == null) {
                    stub = GatewayServiceGrpc.newBlockingStub(channelFactory.getChannel(serviceName));
                }
            }
        }
        return stub;
    }

    /**
     * 推送消息给指定玩家
     */
    public CommonResponse pushToPlayer(PushToPlayerRequest request) {
        try {
            log.info("发送消息给指定玩家: playerId={}, msgId={}", request.getPlayerId(), request.getMessage().getHeader().getMsgId());
            CommonResponse response = getStub().pushToPlayer(request);
            log.info("消息给指定玩家响应: playerId={}, code={}", request.getPlayerId(), response.getCode());
            return response;
        }catch (Exception e){
            log.error("推送消息给指定玩家失败: playerId={}", request.getPlayerId(), e);
            throw new RuntimeException("推送消息给指定玩家服务调用失败", e);
        }
    }

    /**
     * 推送消息给所有在线玩家
     */
    public CommonResponse pushToAll(PushToAllRequest request) {
        try {
            log.info("发送消息给所有玩家: msgId={}", request.getMessage().getHeader().getMsgId());
            CommonResponse response = getStub().pushToAll(request);
            log.info("消息给所有玩家响应: code={}", response.getCode());
            return response;
        }catch (Exception e){
            log.error("推送消息给所有玩家失败", e);
            throw new RuntimeException("推送消息给所有玩家服务调用失败", e);
        }
    }

    /**
     * 推送消息给指定玩家群
     */
    public CommonResponse pushToPlayerGroup(PushToPlayerGroupRequest request) {
        try {
            log.info("发送消息给指定玩家群: playerGroupId={}, msgId={}", request.getGroup().getGroupId(), request.getMessage().getHeader().getMsgId());
            CommonResponse response = getStub().pushToPlayerGroup(request);
            log.info("消息给指定玩家群响应: playerGroupId={}, code={}", request.getGroup().getGroupId(), response.getCode());
            return response;
        }catch (Exception e){
            log.error("推送消息给指定玩家群失败: playerGroupId={}", request.getGroup().getGroupId(), e);
            throw new RuntimeException("推送消息给指定玩家群服务调用失败", e);
        }
    }

}
