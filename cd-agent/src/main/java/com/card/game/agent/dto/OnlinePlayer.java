package com.card.game.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 在线玩家信息（内存缓存）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlinePlayer {

    /** 玩家ID */
    private Long playerId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 所在网关ID */
    private String gatewayId;

    /** 会话ID */
    private String sessionId;

    /** 登录时间 */
    private Long loginTime;

    /** 最后活跃时间 */
    private Long lastActiveTime;

    /** 玩家状态 */
    private Integer status;

    /** 等级 */
    private Integer level;

    /** 头像 */
    private String avatar;
}