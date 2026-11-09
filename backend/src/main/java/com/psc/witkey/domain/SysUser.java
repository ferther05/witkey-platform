package com.psc.witkey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户表：账号信息 + 个人资料（本项目规模不大，资料不单独建表）。
 */
@Entity
@Table(name = "sys_user")
@Getter
@Setter
@NoArgsConstructor
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt 加密后的密码 */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 255)
    private String avatar;

    @Column(length = 255)
    private String intro;

    /** 技能标签，逗号分隔 */
    @Column(length = 255)
    private String skills;

    @Column(length = 20)
    private String mobile;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "identity_type", nullable = false, length = 20)
    private String identityType;

    @Column(name = "auth_status", nullable = false, length = 20)
    private String authStatus = AuthStatus.UNVERIFIED.name();

    @Column(name = "real_name", length = 50)
    private String realName;

    /** 1 正常，0 封禁 */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isBanned() {
        return status == null || status == 0;
    }
}
