package com.card.game.author.service.impl;

import com.card.game.author.dto.LoginResult;
import com.card.game.author.dto.RefreshResult;
import com.card.game.author.dto.RegisterResult;
import com.card.game.author.dto.VerifyResult;
import com.card.game.author.entity.AccountEntity;
import com.card.game.author.enums.AccountStatusEnum;
import com.card.game.author.repository.AccountRepository;
import com.card.game.author.service.AuthService;
import com.card.game.author.service.TokenCacheService;
import com.card.game.author.util.JwtUtils;
import com.card.game.proto.common.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final JwtUtils jwtUtil;
    private final TokenCacheService tokenCacheService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public RegisterResult register(String username, String password, String nickname, String registerIp) {
        // 1. 参数校验
        if (username == null || username.trim().isEmpty()) {
            return RegisterResult.failure(Code.INVALID_PARAM, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return RegisterResult.failure(Code.INVALID_PARAM, "密码不能为空");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = username; // 默认昵称为用户名
        }

        // 2. 检查用户名是否已存在
        if (accountRepository.existsByUsername(username)) {
            return RegisterResult.failure(Code.USER_EXISTS, "用户名已存在");
        }

        // 3. 加密密码
        String encodedPassword = passwordEncoder.encode(password);

        // 4. 创建账号
        try {
            AccountEntity account = AccountEntity.builder()
                    .username(username)
                    .password(encodedPassword)
                    .nickname(nickname)
                    .status(AccountStatusEnum.STATUS_NORMAL.getCode())
                    .registerIp(registerIp)
                    .build();

            AccountEntity saved = accountRepository.save(account);
            long createTime = saved.getCreateTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            log.info("注册成功: playerId={}, username={}", saved.getId(), username);
            return RegisterResult.success(saved.getId(), createTime);

        } catch (Exception e) {
            log.error("注册失败: username={}", username, e);
            return RegisterResult.failure(Code.INTERNAL_ERROR, "注册失败，请稍后重试");
        }
    }

    @Override
    @Transactional
    public LoginResult authenticate(String username, String password, String gatewayId,
                                    int clientType, String clientVersion, String deviceId) {
        // 1. 参数校验
        if (username == null || username.trim().isEmpty()) {
            return LoginResult.failure(Code.INVALID_PARAM, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return LoginResult.failure(Code.INVALID_PARAM, "密码不能为空");
        }

        // 2. 查询用户
        Optional<AccountEntity> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty()) {
            log.warn("登录失败，用户不存在: username={}", username);
            return LoginResult.failure(Code.USER_NOT_FOUND, "用户不存在");
        }

        AccountEntity account = accountOpt.get();

        // 3. 检查账号状态
        if (account.getStatus() == AccountStatusEnum.STATUS_BANNED.getCode()) {
            log.warn("登录失败，账号已封禁: playerId={}", account.getId());
            return LoginResult.failure(Code.USER_BANNED, "账号已被封禁");
        }
        if (account.getStatus() == AccountStatusEnum.STATUS_DELETED.getCode()) {
            return LoginResult.failure(Code.USER_NOT_FOUND, "用户不存在");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(password, account.getPassword())) {
            log.warn("登录失败，密码错误: playerId={}", account.getId());
            return LoginResult.failure(Code.WRONG_PASSWORD, "密码错误");
        }

        // 5. 检查是否已在线（可选：踢下线或拒绝登录）
        String existingToken = tokenCacheService.getPlayerToken(account.getId());
        if (existingToken != null && jwtUtil.validateToken(existingToken)) {
            log.info("用户已在别处登录: playerId={}", account.getId());
            // 可以选择踢下线或直接返回已在线的提示
            // 这里选择先删除旧 Token，允许新登录
            tokenCacheService.deletePlayerToken(account.getId());
        }

        // 6. 生成新 Token
        String token = jwtUtil.generateToken(account.getId(), account.getUsername());
        long expireAt = System.currentTimeMillis() + 7L * 24 * 3600 * 1000;

        // 7. 存储 Token
        tokenCacheService.saveToken(token, account.getId(), account.getUsername());

        // 8. 更新登录信息
        accountRepository.updateLoginInfo(account.getId(), deviceId, LocalDateTime.now());

        log.info("登录成功: playerId={}, username={}, gatewayId={}",
                account.getId(), username, gatewayId);
        return LoginResult.success(token, expireAt, account);
    }

    @Override
    public VerifyResult verifyToken(String token, String playerId) {
        // 1. 检查 Token 是否存在
        if (token == null || token.trim().isEmpty()) {
            return VerifyResult.failure(Code.TOKEN_MISSING, "Token不能为空");
        }

        // 2. 验证 JWT 签名和过期时间
        if (!jwtUtil.validateToken(token)) {
            return VerifyResult.failure(Code.TOKEN_INVALID, "Token无效或已过期");
        }

        // 3. 检查缓存中是否存在
        if (!tokenCacheService.tokenExists(token)) {
            return VerifyResult.failure(Code.TOKEN_INVALID, "Token无效");
        }

        // 4. 获取 Token 中的玩家信息
        String tokenPlayerId = jwtUtil.getPlayerIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);

        if (tokenPlayerId == null) {
            return VerifyResult.failure(Code.TOKEN_INVALID, "Token无效");
        }

        // 5. 如果传入了 playerId，校验是否匹配
        if (playerId != null && !playerId.equals(tokenPlayerId)) {
            return VerifyResult.failure(Code.TOKEN_INVALID, "Token与玩家不匹配");
        }

        Long expireAt = jwtUtil.getExpirationDate(token) != null
                ? jwtUtil.getExpirationDate(token).getTime() : null;

        return VerifyResult.success(tokenPlayerId, username, expireAt);
    }

    @Override
    public RefreshResult refreshToken(String token) {
        // 1. 验证旧 Token
        if (token == null || !jwtUtil.validateToken(token)) {
            return RefreshResult.failure(Code.TOKEN_INVALID, "Token无效");
        }

        // 2. 检查缓存
        if (!tokenCacheService.tokenExists(token)) {
            return RefreshResult.failure(Code.TOKEN_INVALID, "Token无效");
        }

        // 3. 获取玩家信息
        String playerId = jwtUtil.getPlayerIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);

        return RefreshResult.failure(Code.TOKEN_INVALID, "Token无效");

        // 4. 生成新 Token
    }

    @Override
    public boolean logout(String playerId, String token, String reason) {
        if (token != null) {
            tokenCacheService.deleteToken(token);
        }
        if (playerId != null) {
            tokenCacheService.deletePlayerToken(playerId);
        }
        log.info("登出成功: playerId={}, reason={}", playerId, reason);
        return true;
    }

    @Override
    public boolean kickPlayer(String playerId, String kickReason, int kickCode) {
        String token = tokenCacheService.getPlayerToken(playerId);
        if (token != null) {
            tokenCacheService.deleteToken(token);
        }
        tokenCacheService.deletePlayerToken(playerId);
        log.info("踢下线: playerId={}, reason={}, code={}", playerId, kickReason, kickCode);
        return true;
    }
}
