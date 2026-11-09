package com.psc.witkey.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 模拟提现：提交即到账，仅记流水，不接真实支付通道。 */
public record WithdrawRequest(
        @NotNull(message = "请填写提现金额")
        @DecimalMin(value = "1.00", message = "单次提现不低于 1 元")
        BigDecimal amount
) {
}
