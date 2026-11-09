package com.psc.witkey.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 模拟充值：不进真实支付，仅用于演示资金链路。 */
public record RechargeRequest(
        @NotNull(message = "请填写充值金额")
        @DecimalMin(value = "1.00", message = "单次充值不低于 1 元")
        @DecimalMax(value = "100000.00", message = "单次充值不超过 10 万元")
        BigDecimal amount
) {
}
