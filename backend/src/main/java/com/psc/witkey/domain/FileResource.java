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
 * 文件资源：业务表只存 fileId，真实路径/对象键只在这里出现，避免把存储路径暴露给前端。
 */
@Entity
@Table(name = "file_resource", indexes = {
        @Index(name = "idx_file_uploader", columnList = "uploader_id"),
        @Index(name = "idx_file_biz", columnList = "biz_type,biz_id")
})
@Getter
@Setter
@NoArgsConstructor
public class FileResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "object_key", nullable = false, length = 255)
    private String objectKey;

    @Column(length = 64)
    private String bucket;

    @Column(nullable = false)
    private Long size;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    /** AVATAR / TASK / DELIVER / REPORT */
    @Column(name = "biz_type", length = 20)
    private String bizType;

    @Column(name = "biz_id")
    private Long bizId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
