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
 * 站内消息：系统消息与交易消息共用一张表，bizType/bizId 指回业务对象以便前端跳转。
 */
@Entity
@Table(name = "notice", indexes = {
        @Index(name = "idx_notice_user_read", columnList = "user_id,is_read")
})
@Getter
@Setter
@NoArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** SYSTEM 系统 / TRADE 交易 */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    /** 业务类型，如 TASK / ORDER / BID / USER */
    @Column(name = "biz_type", length = 20)
    private String bizType;

    @Column(name = "biz_id")
    private Long bizId;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.read == null) {
            this.read = false;
        }
    }
}
