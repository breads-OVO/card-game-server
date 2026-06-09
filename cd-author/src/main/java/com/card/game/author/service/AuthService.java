package com.card.game.author.service;

import com.card.game.author.dto.LoginResult;
import com.card.game.author.dto.RefreshResult;
import com.card.game.author.dto.RegisterResult;
import com.card.game.author.dto.VerifyResult;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 注册账号
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @param nickname 昵称
     * @param registerIp 注册IP
     * @return 注册结果（包含 Code 和 playerId）
     */
    RegisterResult register(String username, String password, String nickname, String registerIp);

    /**
     * 登录认证
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @param gatewayId 网关ID
     * @param clientType 客户端类型
     * @param clientVersion 客户端版本
     * @param deviceId 设备ID
     * @return 登录结果
     */
    LoginResult authenticate(String username, String password, String gatewayId,
                             int clientType, String clientVersion, String deviceId);

    /**
     * 验证 Token
     *
     * @param token JWT Token
     * @param playerId 玩家ID（可选校验）
     * @return 验证结果
     */
    VerifyResult verifyToken(String token, String playerId);

    /**
     * 刷新 Token
     *
     * @param token 旧 Token
     * @return 新 Token
     */
    RefreshResult refreshToken(String token);

    /**
     * 登出
     *
     * @param playerId 玩家ID
     * @param token JWT Token
     * @param reason 登出原因
     * @return 是否成功
     */
    boolean logout(String playerId, String token, String reason);

    /**
     * 踢玩家下线
     *
     * @param playerId 玩家ID
     * @param kickReason 踢下线原因
     * @param kickCode 踢下线错误码
     * @return 是否成功
     */
    boolean kickPlayer(String playerId, String kickReason, int kickCode);

}
