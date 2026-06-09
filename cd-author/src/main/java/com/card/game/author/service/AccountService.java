package com.card.game.author.service;

import com.card.game.author.entity.AccountEntity;
import com.card.game.author.enums.AccountStatusEnum;
import com.card.game.author.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 账号服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * 创建账号
     */
    @Transactional
    public AccountEntity createAccount(String username, String encodedPassword,
                                       String nickname, String registerIp) {
        // 检查用户名是否已存在
        if (accountRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在: " + username);
        }

        AccountEntity account = AccountEntity.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(nickname)
                .avatar("")
                .status(AccountStatusEnum.STATUS_NORMAL.getCode())
                .registerIp(registerIp)
                .build();

        AccountEntity saved = accountRepository.save(account);
        log.info("创建账号成功: playerId={}, username={}", saved.getId(), username);
        return saved;
    }

    /**
     * 根据用户名查询账号
     */
    public Optional<AccountEntity> findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    /**
     * 根据玩家ID查询账号
     */
    public Optional<AccountEntity> findById(String playerId) {
        return accountRepository.findById(playerId);
    }

    /**
     * 更新最后登录信息
     */
    @Transactional
    public void updateLoginInfo(String playerId, String ip) {
        int updated = accountRepository.updateLoginInfo(playerId, ip, LocalDateTime.now());
        if (updated > 0) {
            log.debug("更新登录信息成功: playerId={}, ip={}", playerId, ip);
        }
    }

    /**
     * 更新账号状态
     */
    @Transactional
    public boolean updateStatus(String playerId, Integer status) {
        int updated = accountRepository.updateStatus(playerId, status);
        if (updated > 0) {
            log.info("更新账号状态: playerId={}, status={}", playerId, status);
            return true;
        }
        return false;
    }

    /**
     * 检查账号是否存在
     */
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }
}
