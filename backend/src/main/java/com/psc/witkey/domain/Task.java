package com.psc.witkey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务主表。分类/雇主只存 id，避免懒加载问题，展示时批量查名字。
 */
@Entity
@Table(name = "task", indexes = {
        @Index(name = "idx_task_status_cat", columnList = "status,category_id,created_at"),
        @Index(name = "idx_task_employer", columnList = "employer_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "employer_id", nullable = false)
    private Long employerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false, length = 20)
    private String status = TaskStatus.PENDING_AUDIT.name();

    @Column(name = "audit_remark", length = 255)
    private String auditRemark;

    @Column(name = "bid_count", nullable = false)
    private Integer bidCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

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
}
