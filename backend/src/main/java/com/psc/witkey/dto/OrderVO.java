package com.psc.witkey.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderVO(
        Long id,
        String orderNo,
        Long taskId,
        String taskTitle,
        String taskDescription,
        Long employerId,
        String employerName,
        Long witkeyId,
        String witkeyName,
        BigDecimal amount,
        BigDecimal commissionRate,
        BigDecimal commissionAmount,
        String status,
        String deliverNote,
        Integer deliverCount,
        String rejectReason,
        java.util.List<FileVO> deliverFiles,
        LocalDateTime deliverTime,
        LocalDateTime acceptTime,
        LocalDateTime settleTime,
        LocalDateTime createdAt
) {
}
