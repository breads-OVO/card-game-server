package com.card.game.agent.service;

import com.card.game.agent.dto.PlayerSession;
import com.card.game.proto.common.PlayerBaseInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 玩家会话管理器接口
 */
public interface PlayerSessionManagerService {

    /**
     * 玩家上线
     * @param session 玩家会话信息
     * @return 是否成功
     */
    boolean playerOnline(PlayerSession session);

    /**
     * 玩家下线
     * @param playerId 玩家ID
     * @param reason 下线原因
     * @return 是否成功
     */
    boolean playerOffline(String playerId, String reason);

    /**
     * 更新心跳
     * @param playerId 玩家ID
     * @return 是否成功
     */
    boolean updateHeartbeat(String playerId);

    /**
     * 获取玩家会话信息
     * @param playerId 玩家ID
     * @return 玩家会话信息
     */
    PlayerSession getPlayerSession(String playerId);

    /**
     * 检查玩家是否在线
     * @param playerId 玩家ID
     * @return 是否在线
     */
    boolean isOnline(String playerId);

    /**
     * 获取在线玩家数量
     * @return 在线玩家数量
     */
    int getOnlinePlayerCount();

    /**
     * 获取所有在线玩家ID
     * @return 在线玩家ID集合
     */
    Set<String> getAllOnlinePlayerIds();

    /**
     * 批量获取玩家会话信息
     * @param playerIds 玩家ID列表
     * @return 玩家会话映射
     */
    Map<String, PlayerSession> batchGetPlayerSessions(List<String> playerIds);

    /**
     * 获取玩家所在的网关ID
     * @param playerId 玩家ID
     * @return 网关ID
     */
    String getPlayerGatewayId(String playerId);

    /**
     * 更新玩家状态
     * @param playerId 玩家ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updatePlayerStatus(String playerId, Integer status);

    /**
     * 踢玩家下线
     * @param playerId 玩家ID
     * @param reason 踢下线原因
     * @return 是否成功
     */
    boolean kickPlayer(String playerId, String reason);

    /**
     * 将 PlayerSession 转换为 gRPC 消息
     */
    com.card.game.proto.common.PlayerSession toGrpcSession(PlayerSession session);

    /**
     * 将 PlayerBaseInfo 转换为内部 PlayerSession
     */
    PlayerSession toPlayerSession(PlayerBaseInfo playerInfo, String gatewayId,
                                  String sessionId, String token);
}