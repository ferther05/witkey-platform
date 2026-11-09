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
 * 担保订单：选标时生成，一个任务最多一个订单（task_id 唯一）。
 * 佣金比例是下单时的快照，之后改分类佣金不影响历史订单。
 */
@Entity
@Table(name = "biz_order",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_no", columnNames = "order_no"),
                @UniqueConstraint(name = "uk_order_task", columnNames = "task_id")
        },
        indexes = {
                @Index(name = "idx_order_employer", columnList = "employer_id"),
                @Index(name = "idx_order_witkey", columnList = "witkey_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class BizOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 32)
    private String orderNo;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "employer_id", nullable = false)
    private Long employerId;

    @Column(name = "witkey_id", nullable = false)
    private Long witkeyId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(nullable = false, length = 20)
    private String status = OrderStatus.TO_DELIVER.name();

    /** 交付说明（本模块先存文本，附件等文件存储模块） */
    @Column(name = "deliver_note", length = 1000)
    private String deliverNote;

    /** 交付附件：逗号分隔的 file_resource id */
    @Column(name = "deliver_file_ids", length = 500)
    private String deliverFileIds;

    @Column(name = "deliver_count", nullable = false)
    private Integer deliverCount = 0;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "deliver_time")
    private LocalDateTime deliverTime;

    @Column(name = "accept_time")
    private LocalDateTime acceptTime;

    @Column(name = "settle_time")
    private LocalDateTime settleTime;

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
