package com.card.game.gateway.service.impl;

import com.card.game.gateway.service.GatewayService;
import com.card.game.gateway.session.ChannelSessionManager;
import com.card.game.proto.common.Code;
import com.card.game.proto.common.CommonResponse;
import com.card.game.proto.common.GameMessage;
import com.card.game.proto.gateway.PlayerGroup;
import com.card.game.proto.gateway.PushToAllRequest;
import com.card.game.proto.gateway.PushToPlayerGroupRequest;
import com.card.game.proto.gateway.PushToPlayerRequest;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class GatewayServiceImpl implements GatewayService {

    @Resource
    private ChannelSessionManager sessionManager;

    @Override
    public CommonResponse pushToPlayer(PushToPlayerRequest request) {
        String playerId = request.getPlayerId();
        GameMessage data = request.getMessage();
        Channel channel = sessionManager.getChannelByPlayerId(playerId);
        if (channel != null) {
            channel.writeAndFlush(data);
            log.info("推送玩家消息: {},消息id:{}", playerId, data.getHeader().getMsgId());
            return CommonResponse.newBuilder()
                    .setCode(Code.SUCCESS)
                    .setMessage("推送成功")
                    .build();
        }else {
            log.info("玩家不存在: {}", playerId);
            return CommonResponse.newBuilder()
                    .setCode(Code.ERROR)
                    .setMessage("玩家不存在")
                    .build();
        }
    }

    @Override
    public CommonResponse pushToAll(PushToAllRequest request) {
        GameMessage data = request.getMessage();
        Iterable<Channel>  channels = sessionManager.getAllChannels();
        try {
            channels.forEach(channel -> {
                channel.writeAndFlush(data);
                log.info("推送玩家消息: 玩家id:{},消息id:{}", sessionManager.getPlayerIdByChannel(channel), data.getHeader().getMsgId());
            });
            log.info("推送所有玩家消息: 消息id:{}", data.getHeader().getMsgId());
            return CommonResponse.newBuilder()
                    .setCode(Code.SUCCESS)
                    .setMessage("推送成功")
                    .build();
        }catch (Exception e){
            log.error("推送所有玩家消息失败: 玩家数量:{},消息id:{}", channels.spliterator().estimateSize(), data.getHeader().getMsgId(), e);
            return CommonResponse.newBuilder()
                    .setCode(Code.ERROR)
                    .setMessage("推送失败")
                    .build();
        }

    }

    @Override
    public CommonResponse pushToPlayerGroup(PushToPlayerGroupRequest request) {
        GameMessage data = request.getMessage();
        PlayerGroup playerGroup = request.getGroup();
        List<String> playerIds = playerGroup.getPlayerIdList();
        Iterable<Channel>  channels = sessionManager.getChannelsByPlayerIds(playerIds);
        try {
            channels.forEach(channel -> {
                channel.writeAndFlush(data);
                log.info("推送玩家群消息: 玩家id:{},消息id:{}", sessionManager.getPlayerIdByChannel(channel), data.getHeader().getMsgId());
            });
            log.info("推送玩家群消息: 玩家群id:{},消息id:{}", playerGroup.getGroupId(), data.getHeader().getMsgId());
            return CommonResponse.newBuilder()
                    .setCode(Code.SUCCESS)
                    .setMessage("推送成功")
                    .build();
        }catch (Exception e){
            log.error("推送玩家群消息失败: 玩家数量:{},消息id:{}", channels.spliterator().estimateSize(), data.getHeader().getMsgId(), e);
            return CommonResponse.newBuilder()
                    .setCode(Code.ERROR)
                    .setMessage("推送失败")
                    .build();
        }
    }
}
