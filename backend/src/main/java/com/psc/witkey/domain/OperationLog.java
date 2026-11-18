// OperationLog —— 实体：操作日志（对应表 operation_log）
package com.psc.witkey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 实体：操作日志（对应表 operation_log），记录"谁在什么时候做了什么"，供管理员审计。
 */
@Entity
@Table(name = "operation_log", indexes = {
        @Index(name = "idx_log_created", columnList = "created_at"),
        @Index(name = "idx_log_user", columnList = "user_id"),
        @Index(name = "idx_log_action", columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作人 id（系统自动触发的记录为空） */
    @Column(name = "user_id")
    private Long userId;

    /** 操作人用户名快照，避免用户改名后历史记录失真 */
    @Column(length = 50)
    private String username;

    /** 操作人角色快照（ADMIN / USER） */
    @Column(length = 20)
    private String role;

    /** 动作编码，如 TASK_PUBLISH、ORDER_ACCEPT */
    @Column(nullable = false, length = 40)
    private String action;

    /** 对象类型：TASK / BID / ORDER / USER / FILE / CATEGORY / REPORT / WALLET / AUTH */
    @Column(name = "target_type", length = 20)
    private String targetType;

    /** 对象 id */
    @Column(name = "target_id")
    private Long targetId;

    /** 简要说明，例如"发布任务《品牌海报设计》赏金 800" */
    @Column(length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
