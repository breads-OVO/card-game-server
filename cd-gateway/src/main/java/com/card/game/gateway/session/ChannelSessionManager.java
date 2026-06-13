package com.card.game.gateway.session;


import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接会话管理器
 * 管理 Channel 与 PlayerId 的映射关系
 */
@Slf4j
@Component
public class ChannelSessionManager {

    /** Channel ID -> Channel */
    private final ConcurrentHashMap<ChannelId, Channel> channels = new ConcurrentHashMap<>();

    /** PlayerId -> Channel */
    private final ConcurrentHashMap<String, Channel> playerChannelMap = new ConcurrentHashMap<>();

    /** Channel -> PlayerId */
    private final ConcurrentHashMap<ChannelId, String> channelPlayerMap = new ConcurrentHashMap<>();

    /** 连接计数器 */
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    /**
     * 注册新连接
     */
    public void registerChannel(Channel channel) {
        channels.put(channel.id(), channel);
        connectionCount.incrementAndGet();
        log.debug("注册连接: {}, 当前连接数: {}", channel.remoteAddress(), connectionCount.get());
    }

    /**
     * 移除连接
     */
    public void removeChannel(Channel channel) {
        ChannelId channelId = channel.id();

        // 移除映射关系
        String playerId = channelPlayerMap.remove(channelId);
        if (playerId != null) {
            playerChannelMap.remove(playerId);
        }

        channels.remove(channelId);
        int remaining = connectionCount.decrementAndGet();

        log.debug("移除连接: {}, playerId={}, 剩余连接数: {}",
                channel.remoteAddress(), playerId, remaining);
    }

    /**
     * 绑定玩家到连接
     */
    public void bindPlayer(Channel channel, String playerId) {
        ChannelId channelId = channel.id();

        // 移除旧的绑定（如果存在）
        String oldPlayerId = channelPlayerMap.get(channelId);
        if (oldPlayerId != null) {
            playerChannelMap.remove(oldPlayerId);
        }

        // 建立新绑定
        channelPlayerMap.put(channelId, playerId);
        playerChannelMap.put(playerId, channel);

        log.debug("绑定玩家: playerId={}, channel={}", playerId, channel.remoteAddress());
    }

    /**
     * 解绑玩家
     */
    public void unbindPlayer(String playerId) {
        Channel channel = playerChannelMap.remove(playerId);
        if (channel != null) {
            channelPlayerMap.remove(channel.id());
            log.debug("解绑玩家: playerId={}", playerId);
        }
    }

    /**
     * 根据玩家ID获取连接
     */
    public Channel getChannelByPlayerId(String playerId) {
        return playerChannelMap.get(playerId);
    }

    /**
     * 获取所有连接
     */
    public Iterable<Channel> getAllChannels() {
        return channels.values();
    }

    /**
     * 根据玩家id列表获取连接
     */
    public Iterable<Channel> getChannelsByPlayerIds(List<String> playerIds) {
        return playerIds.stream()
                .map(playerChannelMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 根据连接获取玩家ID
     */
    public String getPlayerIdByChannel(Channel channel) {
        return channelPlayerMap.get(channel.id());
    }

    /**
     * 获取当前连接数
     */
    public int getChannelCount() {
        return connectionCount.get();
    }

    /**
     * 获取在线玩家数
     */
    public int getOnlinePlayerCount() {
        return playerChannelMap.size();
    }

    /**
     * 判断玩家是否在线
     */
    public boolean isPlayerOnline(String playerId) {
        return playerChannelMap.containsKey(playerId);
    }

    /**
     * 判断连接是否存在
     */
    public boolean isChannelActive(Channel channel) {
        return channel != null && channel.isActive() && channels.containsKey(channel.id());
    }

    /**
     * 踢掉指定玩家的连接
     */
    public void kickPlayer(String playerId) {
        Channel channel = playerChannelMap.get(playerId);
        if (channel != null && channel.isActive()) {
            channel.close();
            log.info("踢掉玩家连接: playerId={}", playerId);
        }
    }

    /**
     * 获取统计信息
     */
    public Stats getStats() {
        return new Stats(
                connectionCount.get(),
                playerChannelMap.size(),
                channels.size()
        );
    }

    /**
     * 统计信息类
     */
    public record Stats(int totalConnections, int onlinePlayers, int registeredChannels) {
        @Override
        public String toString() {
            return String.format("Stats{connections=%d, players=%d, channels=%d}",
                    totalConnections, onlinePlayers, registeredChannels);
        }
    }
}
