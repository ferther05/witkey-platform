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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标：同一任务同一威客只能投一次（唯一索引兜底）。
 */
@Entity
@Table(name = "bid",
        uniqueConstraints = @UniqueConstraint(name = "uk_bid_task_witkey", columnNames = {"task_id", "witkey_id"}),
        indexes = @Index(name = "idx_bid_task_status", columnList = "task_id,status"))
@Getter
@Setter
@NoArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "witkey_id", nullable = false)
    private Long witkeyId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quote;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false, length = 20)
    private String status = BidStatus.PENDING.name();

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
