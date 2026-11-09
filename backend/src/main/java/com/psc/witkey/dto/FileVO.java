package com.psc.witkey.dto;

import com.psc.witkey.domain.FileResource;

import java.time.LocalDateTime;

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
        LocalDateTime createdAt
) {

    public static FileVO from(FileResource f) {
        String type = f.getContentType() == null ? "" : f.getContentType();
        return new FileVO(f.getId(), f.getOriginalName(), f.getSize(), f.getContentType(),
                f.getBizType(), f.getBizId(), f.getUploaderId(),
                "/api/file/" + f.getId() + "/preview",
                type.startsWith("image/"), f.getCreatedAt());
    }
}
