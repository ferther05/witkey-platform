package com.psc.witkey.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReportVO(
        Long id,
        Long reporterId,
        String reporterName,
        String targetType,
        Long targetId,
        String targetTitle,
        String reason,
        List<FileVO> files,
        String status,
        String handleRemark,
        String handleOrderAction,
        LocalDateTime createdAt,
        LocalDateTime handledAt
) {
}
