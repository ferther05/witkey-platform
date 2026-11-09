package com.psc.witkey.dto;

import java.math.BigDecimal;

/**
 * 首页公开展示的平台概览，不含任何用户隐私数据。
 */
public record PublicOverviewVO(
        long taskCount,
        long recruitingCount,
        long userCount,
        long orderCount,
        long doneOrderCount,
        BigDecimal gmv,
        BigDecimal commission
) {
}
