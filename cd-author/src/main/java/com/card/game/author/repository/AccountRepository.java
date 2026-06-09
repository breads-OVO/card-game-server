package com.card.game.author.repository;

import com.card.game.author.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 账号数据访问层
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    /**
     * 根据用户名查询账号
     */
    Optional<AccountEntity> findByUsername(String username);

    /**
     * 根据用户名检查账号是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据昵称查询账号
     */
    Optional<AccountEntity> findByNickname(String nickname);

    /**
     * 更新最后登录信息
     */
    @Modifying
    @Transactional
    @Query("UPDATE AccountEntity a SET a.lastLoginIp = :ip, a.lastLoginTime = :loginTime WHERE a.id = :playerId")
    int updateLoginInfo(@Param("playerId") String playerId,
                        @Param("ip") String ip,
                        @Param("loginTime") LocalDateTime loginTime);

    /**
     * 更新账号状态
     */
    @Modifying
    @Transactional
    @Query("UPDATE AccountEntity a SET a.status = :status WHERE a.id = :playerId")
    int updateStatus(@Param("playerId") String playerId, @Param("status") Integer status);

    /**
     * 根据状态查询数量（用于统计）
     */
    long countByStatus(Integer status);
}
