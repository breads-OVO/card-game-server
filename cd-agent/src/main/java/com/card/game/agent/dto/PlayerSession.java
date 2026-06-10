package com.card.game.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 玩家会话
 * 存储在 Redis 和内存中的玩家会话信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 玩家ID */
    private String playerId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 所在网关ID */
    private String gatewayId;

    /** 会话ID */
    private String sessionId;

    /** 登录时间戳 */
    private Long loginTime;

    /** 最后心跳时间戳 */
    private Long lastHeartbeat;

    /** 玩家状态: 0-在线, 1-战斗中, 2-匹配中 */
    private Integer status;

    /** Token */
    private String token;

    /**
     * 更新心跳时间
     */
    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    /**
     * 检查会话是否过期
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否过期
     */
    public boolean isExpired(int timeoutSeconds) {
        if (lastHeartbeat == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        return (now - lastHeartbeat) > timeoutSeconds * 1000L;
    }
}
