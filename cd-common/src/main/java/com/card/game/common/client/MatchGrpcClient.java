package com.card.game.common.client;

import com.card.game.common.etcd.GrpcChannelFactory;
import com.card.game.proto.match.MatchCommonRequest;
import com.card.game.proto.match.MatchCommonResponse;
import com.card.game.proto.match.MatchServiceGrpc;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchGrpcClient {

    private final GrpcChannelFactory channelFactory;
    private final String serviceName;
    private volatile MatchServiceGrpc.MatchServiceBlockingStub stub;

    public MatchGrpcClient(GrpcChannelFactory channelFactory, String serviceName) {
        this.channelFactory = channelFactory;
        this.serviceName = serviceName;
    }

    private MatchServiceGrpc.MatchServiceBlockingStub getStub() {
        if (stub == null) {
            synchronized (this) {
                if (stub == null) {
                    stub = MatchServiceGrpc.newBlockingStub(channelFactory.getChannel(serviceName));
                }
            }
        }
        return stub;
    }

    /**
     * 通用匹配
     */
    public MatchCommonResponse matchCommon(MatchCommonRequest request) {
        try {
            log.info("发送通用匹配请求: playerId={}", request.getPlayerId());
            MatchCommonResponse response = getStub().matchCommon(request);
            log.info("通用匹配响应: playerId={}, status={}", request.getPlayerId(), response.getStatus());
            return response;
        } catch (Exception e) {
            log.error("发送通用匹配请求失败: playerId={}", request.getPlayerId(), e);
            throw new RuntimeException("发送通用匹配请求服务调用失败", e);
        }
    }
}
