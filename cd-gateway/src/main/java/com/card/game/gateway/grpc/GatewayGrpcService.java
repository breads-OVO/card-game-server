package com.card.game.gateway.grpc;
import com.card.game.gateway.service.GatewayService;
import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.gateway.GatewayServiceGrpc;
import com.card.game.proto.gateway.PushToAllRequest;
import com.card.game.proto.gateway.PushToPlayerGroupRequest;
import com.card.game.proto.gateway.PushToPlayerRequest;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.annotation.Resource;

/**
 * Gateway gRPC 实现
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GatewayGrpcService extends GatewayServiceGrpc.GatewayServiceImplBase{

    @Resource
    private GatewayService gatewayService;

    /**
     * 推送消息给指定玩家
     */
    @Override
    public void pushToPlayer(PushToPlayerRequest request, StreamObserver<CommonResponse> responseObserver){
        CommonResponse response = gatewayService.pushToPlayer(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 推送消息给所有在线玩家
     */
    @Override
    public void pushToAll(PushToAllRequest request, StreamObserver<CommonResponse> responseObserver){
        CommonResponse response = gatewayService.pushToAll(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 推送消息给指定玩家组
     */
    @Override
    public void pushToPlayerGroup(PushToPlayerGroupRequest request, StreamObserver<CommonResponse> responseObserver){
        CommonResponse response = gatewayService.pushToPlayerGroup(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
