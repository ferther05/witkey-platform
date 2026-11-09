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
 * 举报：平台唯一的内容治理入口。
 * 因为没做独立的争议仲裁模块，举报处理时可以对订单执行"强制退款 / 强制打款"，给纠纷留出口。
 */
@Entity
@Table(name = "report", indexes = @Index(name = "idx_report_status", columnList = "status,created_at"))
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    /** TASK / USER / ORDER */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 500)
    private String reason;

    /** 证据文件 id，逗号分隔 */
    @Column(name = "file_ids", length = 500)
    private String fileIds;

    /** PENDING / DONE / REJECTED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "handle_remark", length = 500)
    private String handleRemark;

    /** REFUND / PAY / NONE */
    @Column(name = "handle_order_action", length = 20)
    private String handleOrderAction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
