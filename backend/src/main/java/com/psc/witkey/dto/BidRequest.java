package com.psc.witkey.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BidRequest(
        @NotNull(message = "缺少任务 id") Long taskId,

        @NotNull(message = "请填写报价")
        @DecimalMin(value = "1.00", message = "报价不能低于 1 元")
        BigDecimal quote,

        @NotBlank(message = "方案说明不能为空")
        @Size(max = 2000, message = "方案说明最长 2000 个字符")
        String content
) {
}
