package com.card.game.author.entity;

import com.card.game.common.db.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 账号实体类
 * 对应数据库 account 表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "account",
        indexes = {
                @Index(name = "idx_username", columnList = "username"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_create_time", columnList = "create_time")
        })
public class AccountEntity extends BaseEntity {

    /** 用户名（唯一） */
    @Column(name = "username", nullable = false, unique = true, length = 32)
    private String username;

    /** 密码（BCrypt 加密） */
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    /** 昵称 */
    @Column(name = "nickname", nullable = false, length = 32)
    private String nickname;

    /** 头像URL */
    @Column(name = "avatar", length = 256)
    private String avatar;

    /** 账号状态：0-正常，1-封禁，2-删除 */
    @Column(name = "status", nullable = false)
    private Integer status;

    /** 最后登录IP */
    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    /** 最后登录时间 */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /** 注册IP */
    @Column(name = "register_ip", length = 64)
    private String registerIp;

    /** 备注 */
    @Column(name = "remark", length = 256)
    private String remark;


}
