package com.psc.witkey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水。biz_no 唯一，是结算/退款幂等的唯一手段。
 */
@Entity
@Table(name = "wallet_flow",
        uniqueConstraints = @UniqueConstraint(name = "uk_flow_biz_no", columnNames = "biz_no"),
        indexes = @Index(name = "idx_flow_user_time", columnList = "user_id,created_at"))
@Getter
@Setter
@NoArgsConstructor
public class WalletFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "biz_no", nullable = false, length = 64)
    private String bizNo;

    /** RECHARGE / FREEZE / UNFREEZE / SETTLE / COMMISSION / WITHDRAW */
    @Column(nullable = false, length = 20)
    private String type;

    /** 正数入账，负数出账 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
