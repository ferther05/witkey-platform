package com.psc.witkey.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 后台数据看板：概览卡 + 近 7 天趋势 + 分类分布。
 */
public record DashboardVO(
        long userCount,
        long taskCount,
        long orderCount,
        long doneOrderCount,
        long pendingAuditTask,
        long pendingReport,
        BigDecimal gmv,
        BigDecimal commission,
        BigDecimal escrow,
        List<TrendPoint> trend,
        List<CategoryStat> categoryStats
) {

    public record TrendPoint(String date, long orders, BigDecimal amount) {
    }

    public record CategoryStat(String name, long count) {
    }
}
