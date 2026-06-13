package com.card.game.match.grpc;

import com.card.game.match.service.MatchService;
import com.card.game.proto.match.*;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * MatchService gRPC 实现
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class MatchServiceGrpcServiceImpl extends MatchServiceGrpc.MatchServiceImplBase {
    @Resource
    private MatchService matchService;

    @Override
    public void matchCommon(MatchCommonRequest request, StreamObserver<MatchCommonResponse> responseObserver){
        MatchCommonResponse response = matchService.matchCommon(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
