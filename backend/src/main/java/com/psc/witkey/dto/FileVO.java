// FileVO —— 响应体：文件元信息（名称、大小、类型、访问地址）
package com.psc.witkey.dto;

import com.psc.witkey.domain.FileResource;

import java.time.LocalDateTime;

/**
 * 响应体：文件元信息（名称、大小、类型、访问地址）。
 */
public record FileVO(
        Long id,
        String name,
        Long size,
        String contentType,
        String bizType,
        Long bizId,
        Long uploaderId,
        String url,
        boolean image,
        LocalDateTime createdAt,
        String uploaderName,
        String storageInfo
) {

    // 实体转响应对象
    public static FileVO from(FileResource f) {
        String type = f.getContentType() == null ? "" : f.getContentType();
        return new FileVO(f.getId(), f.getOriginalName(), f.getSize(), f.getContentType(),
                f.getBizType(), f.getBizId(), f.getUploaderId(),
                "/api/file/" + f.getId() + "/preview",
                type.startsWith("image/"), f.getCreatedAt(), null,
                (f.getBucket() == null ? "local" : f.getBucket()) + " / " + f.getObjectKey());
    }

    /** 由实体转 VO（供任务附件等场景复用） */
    public static FileVO of(com.psc.witkey.domain.FileResource f) {
        String type = f.getContentType() == null ? "" : f.getContentType();
        return new FileVO(f.getId(), f.getOriginalName(), f.getSize(), f.getContentType(),
                f.getBizType(), f.getBizId(), f.getUploaderId(),
                "/api/file/" + f.getId() + "/preview",
                type.startsWith("image/"), f.getCreatedAt(), null,
                (f.getBucket() == null ? "local" : f.getBucket()) + " / " + f.getObjectKey());
    }
}
