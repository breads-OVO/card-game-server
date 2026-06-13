package com.card.game.gateway.service;

import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.gateway.PushToAllRequest;
import com.card.game.proto.gateway.PushToPlayerGroupRequest;
import com.card.game.proto.gateway.PushToPlayerRequest;

public interface GatewayService {

    // 推送消息给指定玩家
    CommonResponse pushToPlayer (PushToPlayerRequest request);

    // 推送消息给所有在线玩家
    CommonResponse pushToAll (PushToAllRequest request);

    // 推送消息给指定玩家组
    CommonResponse pushToPlayerGroup (PushToPlayerGroupRequest request);
}
