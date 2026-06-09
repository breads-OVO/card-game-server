package com.card.game.author.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 Token
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${app.jwt.secret:game-login-secret-key-2024}")
    private String secret;

    @Value("${app.jwt.expire-days:7}")
    private int expireDays;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     *
     * @param playerId 玩家ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(String playerId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("playerId", playerId);
        claims.put("username", username);
        claims.put("type", "access");

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireDays * 24L * 3600 * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(playerId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token JWT Token
     * @return Claims，无效时返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        // 检查是否过期
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return false;
        }
        return !expiration.before(new Date());
    }

    /**
     * 从 Token 获取玩家ID
     */
    public String getPlayerIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object playerId = claims.get("playerId");
        if (playerId instanceof Number) {
            return playerId.toString();
        }
        return null;
    }

    /**
     * 从 Token 获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 获取 Token 过期时间
     */
    public Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 获取 Token 剩余有效时间（毫秒）
     */
    public long getRemainingMs(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return 0;
        }
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return 0;
        }
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
