package com.psc.witkey.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标视图：带上任务标题和威客信息，雇主对比时不用再查。
 * doneCount = 该威客历史完成订单数，作为经验参考。
 */
public record BidVO(
        Long id,
        Long taskId,
        String taskTitle,
        BigDecimal budget,
        Long witkeyId,
        String witkeyName,
        String witkeySkills,
        long doneCount,
        BigDecimal quote,
        String content,
        String status,
        Long orderId,
        LocalDateTime createdAt
) {
}
