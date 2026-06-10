package com.card.game.agent.service.impl;


import com.card.game.agent.config.AgentConfig;
import com.card.game.agent.dto.PlayerSession;
import com.card.game.agent.service.PlayerSessionManagerService;
import com.card.game.proto.common.PlayerBaseInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 玩家会话管理器实现
 * 设计说明：
 * - 内存存储：高性能读写，存储在线玩家完整信息
 * - Redis 存储：跨实例共享，用于服务发现和故障转移
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerSessionManagerServiceImpl implements PlayerSessionManagerService {

    private static final String REDIS_KEY_SESSION = "game:agent:session:";
    private static final String REDIS_KEY_GATEWAY_MAPPING = "game:gateway:player:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AgentConfig agentConfig;

    /** 内存中的在线玩家缓存 */
    private final ConcurrentHashMap<String, PlayerSession> onlinePlayers = new ConcurrentHashMap<>();

    /** 定时任务执行器（用于清理过期会话） */
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // 每30秒执行一次过期会话清理
        scheduler.scheduleAtFixedRate(this::cleanExpiredSessions, 60, 30, TimeUnit.SECONDS);
        log.info("PlayerSessionManager 初始化完成，实例ID: {}", agentConfig.getInstanceId());
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("PlayerSessionManager 已关闭");
    }

    @Override
    public boolean playerOnline(PlayerSession session) {
        if (session == null || session.getPlayerId() == null) {
            log.warn("playerOnline 参数无效");
            return false;
        }

        String playerId = session.getPlayerId();

        // 检查是否已在线
        if (onlinePlayers.containsKey(playerId)) {
            log.warn("玩家已在当前实例在线: playerId={}", playerId);
            // 可以选择踢掉旧会话
            playerOffline(playerId, "duplicate_login");
        }

        // 存储到内存
        session.updateHeartbeat();
        onlinePlayers.put(playerId, session);

        // 存储到 Redis（用于跨实例查询）
        String redisKey = REDIS_KEY_SESSION + playerId;
        redisTemplate.opsForValue().set(redisKey, session,
                agentConfig.getSession().getExpireSeconds(), TimeUnit.SECONDS);

        // 存储网关映射
        String gatewayKey = REDIS_KEY_GATEWAY_MAPPING + playerId;
        redisTemplate.opsForValue().set(gatewayKey, session.getGatewayId(),
                agentConfig.getSession().getExpireSeconds(), TimeUnit.SECONDS);

        log.info("玩家上线: playerId={}, username={}, gatewayId={}, 当前在线数={}",
                playerId, session.getUsername(), session.getGatewayId(), onlinePlayers.size());
        return true;
    }

    @Override
    public boolean playerOffline(String playerId, String reason) {
        if (playerId == null) {
            return false;
        }

        PlayerSession removed = onlinePlayers.remove(playerId);

        // 从 Redis 删除
        String redisKey = REDIS_KEY_SESSION + playerId;
        redisTemplate.delete(redisKey);

        String gatewayKey = REDIS_KEY_GATEWAY_MAPPING + playerId;
        redisTemplate.delete(gatewayKey);

        if (removed != null) {
            log.info("玩家下线: playerId={}, username={}, reason={}, 当前在线数={}",
                    playerId, removed.getUsername(), reason, onlinePlayers.size());
        } else {
            log.debug("玩家下线（不在当前实例）: playerId={}, reason={}", playerId, reason);
        }

        return true;
    }

    @Override
    public boolean updateHeartbeat(String playerId) {
        PlayerSession session = onlinePlayers.get(playerId);
        if (session == null) {
            // 尝试从 Redis 恢复
            PlayerSession redisSession = getPlayerSession(playerId);
            if (redisSession != null) {
                onlinePlayers.put(playerId, redisSession);
                session = redisSession;
            } else {
                return false;
            }
        }

        session.updateHeartbeat();

        // 更新 Redis 中的过期时间
        String redisKey = REDIS_KEY_SESSION + playerId;
        redisTemplate.expire(redisKey, agentConfig.getSession().getExpireSeconds(), TimeUnit.SECONDS);

        String gatewayKey = REDIS_KEY_GATEWAY_MAPPING + playerId;
        redisTemplate.expire(gatewayKey, agentConfig.getSession().getExpireSeconds(), TimeUnit.SECONDS);

        return true;
    }

    @Override
    public PlayerSession getPlayerSession(String playerId) {
        // 先从内存获取
        PlayerSession session = onlinePlayers.get(playerId);
        if (session != null) {
            return session;
        }

        // 从 Redis 获取
        String redisKey = REDIS_KEY_SESSION + playerId;
        Object obj = redisTemplate.opsForValue().get(redisKey);
        if (obj instanceof PlayerSession) {
            return (PlayerSession) obj;
        }

        return null;
    }

    @Override
    public boolean isOnline(String playerId) {
        return onlinePlayers.containsKey(playerId) || getPlayerSession(playerId) != null;
    }

    @Override
    public int getOnlinePlayerCount() {
        return onlinePlayers.size();
    }

    @Override
    public Set<String> getAllOnlinePlayerIds() {
        return new HashSet<>(onlinePlayers.keySet());
    }

    @Override
    public Map<String, PlayerSession> batchGetPlayerSessions(List<String> playerIds) {
        Map<String, PlayerSession> result = new HashMap<>();

        List<String> missingIds = new ArrayList<>();

        // 先从内存获取
        for (String playerId : playerIds) {
            PlayerSession session = onlinePlayers.get(playerId);
            if (session != null) {
                result.put(playerId, session);
            } else {
                missingIds.add(playerId);
            }
        }

        // 从 Redis 批量获取缺失的
        if (!missingIds.isEmpty()) {
            List<String> redisKeys = missingIds.stream()
                    .map(id -> REDIS_KEY_SESSION + id)
                    .collect(Collectors.toList());

            List<Object> values = redisTemplate.opsForValue().multiGet(redisKeys);
            if (values != null) {
                for (int i = 0; i < missingIds.size(); i++) {
                    String playerId = missingIds.get(i);
                    Object obj = values.get(i);
                    if (obj instanceof PlayerSession) {
                        result.put(playerId, (PlayerSession) obj);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public String getPlayerGatewayId(String playerId) {
        PlayerSession session = getPlayerSession(playerId);
        return session != null ? session.getGatewayId() : null;
    }

    @Override
    public boolean updatePlayerStatus(String playerId, Integer status) {
        PlayerSession session = onlinePlayers.get(playerId);
        if (session == null) {
            session = getPlayerSession(playerId);
            if (session == null) {
                return false;
            }
            onlinePlayers.put(playerId, session);
        }

        session.setStatus(status);

        // 更新 Redis
        String redisKey = REDIS_KEY_SESSION + playerId;
        redisTemplate.opsForValue().set(redisKey, session,
                agentConfig.getSession().getExpireSeconds(), TimeUnit.SECONDS);

        return true;
    }

    @Override
    public boolean kickPlayer(String playerId, String reason) {
        return playerOffline(playerId, "kick: " + reason);
    }

    @Override
    public com.card.game.proto.common.PlayerSession toGrpcSession(PlayerSession session) {
        if (session == null) {
            return null;
        }
        return com.card.game.proto.common.PlayerSession.newBuilder()
                .setPlayerId(session.getPlayerId())
                .setGatewayId(session.getGatewayId() != null ? session.getGatewayId() : "")
                .setSessionId(session.getSessionId() != null ? session.getSessionId() : "")
                .setLoginTime(session.getLoginTime() != null ? session.getLoginTime() : 0)
                .setLastHeartbeat(session.getLastHeartbeat() != null ? session.getLastHeartbeat() : 0)
                .setStatus(session.getStatus() != null ? session.getStatus() : 0)
                .build();
    }

    @Override
    public PlayerSession toPlayerSession(PlayerBaseInfo playerInfo, String gatewayId,
                                         String sessionId, String token) {
        if (playerInfo == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        return PlayerSession.builder()
                .playerId(playerInfo.getPlayerId())
                .username(playerInfo.getUsername())
                .nickname(playerInfo.getNickname())
                .gatewayId(gatewayId)
                .sessionId(sessionId)
                .loginTime(now)
                .lastHeartbeat(now)
                .status(0)  // 在线
                .token(token)
                .build();
    }

    /**
     * 清理过期的会话
     */
    private void cleanExpiredSessions() {
        int timeoutSeconds = agentConfig.getSession().getExpireSeconds();
        List<String> expiredPlayers = new ArrayList<>();

        for (Map.Entry<String, PlayerSession> entry : onlinePlayers.entrySet()) {
            PlayerSession session = entry.getValue();
            if (session.isExpired(timeoutSeconds)) {
                expiredPlayers.add(entry.getKey());
            }
        }

        for (String playerId : expiredPlayers) {
            playerOffline(playerId, "heartbeat_timeout");
            log.debug("过期会话已清理: playerId={}", playerId);
        }

        if (!expiredPlayers.isEmpty()) {
            log.info("清理过期会话: {} 个", expiredPlayers.size());
        }
    }
}