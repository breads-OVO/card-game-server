package com.card.game.agent.service;


import com.card.game.proto.agent.ForwardMessageResponse;

/**
 * 消息转发服务接口
 */
public interface MessageForwardService {

    /**
     * 处理转发消息
     * @param playerId 玩家ID
     * @param msgId 消息ID
     * @param body 消息体
     * @param timestamp 时间戳
     * @param seqId 序列号
     * @return 转发响应
     */
    ForwardMessageResponse processMessage(String playerId, int msgId,
                                          byte[] body, long timestamp, int seqId);
}
