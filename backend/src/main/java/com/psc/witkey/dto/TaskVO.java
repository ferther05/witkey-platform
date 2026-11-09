package com.psc.witkey.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务视图对象：把分类名、雇主昵称、是否收藏一起返回，前端不用二次请求。
 */
public record TaskVO(
        Long id,
        String title,
        Long categoryId,
        String categoryName,
        Long employerId,
        String employerName,
        String description,
        BigDecimal budget,
        LocalDateTime deadline,
        String status,
        String auditRemark,
        Integer bidCount,
        Integer viewCount,
        Boolean favorite,
        LocalDateTime createdAt
) {
}
