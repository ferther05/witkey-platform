package com.psc.witkey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务分类。项目只做一级分类，parent_id 预留扩展。
 */
@Entity
@Table(name = "task_category")
@Getter
@Setter
@NoArgsConstructor
public class TaskCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    @Column(nullable = false)
    private Integer sort = 0;

    /** 1 启用 0 停用 */
    @Column(nullable = false)
    private Integer status = 1;

    /** 平台抽佣百分比 */
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("10.00");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
