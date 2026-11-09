package com.psc.witkey.dto;

import java.math.BigDecimal;

/**
 * 对账结果：用流水反推余额，和账户上的实际余额比对。
 * consistent=false 说明有绕过流水改余额的地方（正常应该永远为 true）。
 */
public record ReconcileVO(
        BigDecimal balance,
        BigDecimal frozen,
        BigDecimal computedBalance,
        BigDecimal computedFrozen,
        boolean consistent,
        BigDecimal totalRecharge,
        BigDecimal totalIncome,
        BigDecimal totalWithdraw
) {
}
