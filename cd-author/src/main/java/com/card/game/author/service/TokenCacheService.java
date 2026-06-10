package com.card.game.author.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 缓存服务
 * 专门用于 Token 的存储和管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private static final String TOKEN_PREFIX = "game:token:";
    private static final String PLAYER_TOKEN_PREFIX = "game:player:token:";
    private static final int TOKEN_EXPIRE_DAYS = 7;
    private static final int TOKEN_EXPIRE_SECONDS = TOKEN_EXPIRE_DAYS * 24 * 3600;

    private final com.card.game.common.service.RedisService redisService;

    /**
     * 存储 Token（存储 playerId 和 username）
     */
    public void saveToken(String token, String playerId, String username) {
        String key = TOKEN_PREFIX + token;
        String value = playerId + ":" + username;
        redisService.setEx(key, value, TOKEN_EXPIRE_SECONDS);

        // 同时存储玩家当前的 Token（用于踢下线场景）
        String playerKey = PLAYER_TOKEN_PREFIX + playerId;
        redisService.setEx(playerKey, token, TOKEN_EXPIRE_SECONDS);

        log.debug("Token 已存储: playerId={}, token={}", playerId, maskToken(token));
    }

    /**
     * 获取 Token 对应的玩家信息
     * @return [playerId, username] 或 null
     */
    public String[] getTokenInfo(String token) {
        String key = TOKEN_PREFIX + token;
        String value = redisService.get(key);
        if (value == null) {
            return null;
        }
        String[] parts = value.split(":", 2);
        return parts.length == 2 ? parts : null;
    }

    /**
     * 获取 Token 对应的玩家ID
     */
    public Long getPlayerIdByToken(String token) {
        String[] info = getTokenInfo(token);
        return info != null ? Long.parseLong(info[0]) : null;
    }

    /**
     * 获取 Token 对应的用户名
     */
    public String getUsernameByToken(String token) {
        String[] info = getTokenInfo(token);
        return info != null ? info[1] : null;
    }

    /**
     * 检查 Token 是否存在
     */
    public boolean tokenExists(String token) {
        return redisService.exists(TOKEN_PREFIX + token);
    }

    /**
     * 刷新 Token 过期时间
     */
    public void refreshTokenExpire(String token) {
        String key = TOKEN_PREFIX + token;
        redisService.expire(key, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 获取 playerId 并刷新玩家 Token 映射
        Long playerId = getPlayerIdByToken(token);
        if (playerId != null) {
            String playerKey = PLAYER_TOKEN_PREFIX + playerId;
            redisService.expire(playerKey, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        log.debug("Token 已刷新: {}", maskToken(token));
    }

    /**
     * 删除 Token
     */
    public void deleteToken(String token) {
        Long playerId = getPlayerIdByToken(token);

        String key = TOKEN_PREFIX + token;
        redisService.delete(key);

        if (playerId != null) {
            String playerKey = PLAYER_TOKEN_PREFIX + playerId;
            redisService.delete(playerKey);
        }

        log.debug("Token 已删除: {}", maskToken(token));
    }

    /**
     * 删除玩家所有 Token（用于踢下线）
     */
    public void deletePlayerToken(String playerId) {
        String playerKey = PLAYER_TOKEN_PREFIX + playerId;
        String token = redisService.get(playerKey);
        if (token != null) {
            deleteToken(token);
        }
        log.info("玩家 Token 已清除: playerId={}", playerId);
    }

    /**
     * 获取玩家当前 Token
     */
    public String getPlayerToken(String playerId) {
        String playerKey = PLAYER_TOKEN_PREFIX + playerId;
        return redisService.get(playerKey);
    }

    /**
     * 隐藏 Token（用于日志输出）
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
