package com.psc.witkey.dto;

import java.math.BigDecimal;

/** 后台财务总览：平台视角的资金口径。 */
public record FinanceSummaryVO(
        BigDecimal totalGmv,          // 已完成订单的成交额
        BigDecimal totalCommission,   // 平台累计佣金
        BigDecimal escrowAmount,      // 当前托管中（所有用户冻结之和）
        BigDecimal totalRecharge,     // 平台累计充值
        BigDecimal totalWithdraw,     // 平台累计提现
        long doneOrderCount,
        long activeOrderCount,
        long pendingAuditTaskCount
) {
}
