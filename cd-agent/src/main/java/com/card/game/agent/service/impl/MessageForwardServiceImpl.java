package com.card.game.agent.service.impl;

import com.card.game.agent.service.MessageForwardService;
import com.card.game.agent.service.PlayerSessionManagerService;
import com.card.game.proto.agent.ForwardMessageResponse;
import com.card.game.proto.common.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 消息转发服务实现
 * V1 版本主要做透传和日志记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageForwardServiceImpl implements MessageForwardService {

    @Resource
    private  PlayerSessionManagerService sessionManager;

    @Override
    public ForwardMessageResponse processMessage(String playerId, int msgId,
                                                 byte[] body, long timestamp, int seqId) {
        log.debug("收到业务消息: playerId={}, msgId={}, seqId={}, bodySize={}",
                playerId, msgId, seqId, body != null ? body.length : 0);

        // 检查玩家是否在线
        if (!sessionManager.isOnline(playerId)) {
            log.warn("玩家不在线，无法处理消息: playerId={}", playerId);
            return ForwardMessageResponse.newBuilder()
                    .setCode(Code.SESSION_NOT_FOUND)
                    .setMessage("玩家不在线")
                    .setMsgId(msgId)
                    .setTimestamp(System.currentTimeMillis())
                    .build();
        }

        // 更新心跳
        sessionManager.updateHeartbeat(playerId);

        // TODO: V2 版本实现具体的业务消息分发
        // 根据 msgId 调用对应的业务处理器

        // V1 版本：返回成功，不做具体业务处理
        return ForwardMessageResponse.newBuilder()
                .setCode(Code.SUCCESS)
                .setMessage("消息已接收")
                .setMsgId(msgId)
                .setTimestamp(System.currentTimeMillis())
                .build();
    }
}
