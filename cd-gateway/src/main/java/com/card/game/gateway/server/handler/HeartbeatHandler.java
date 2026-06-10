package com.card.game.gateway.server.handler;

import com.card.game.gateway.session.ChannelSessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 心跳处理器
 * 处理空闲超时和心跳消息
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Resource
    private ChannelSessionManager sessionManager;
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲超时，关闭连接
                String clientAddr = ctx.channel().remoteAddress().toString();
                String playerId = sessionManager.getPlayerIdByChannel(ctx.channel());
                log.warn("读空闲超时，关闭连接: {}, playerId={}", clientAddr, playerId);
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}