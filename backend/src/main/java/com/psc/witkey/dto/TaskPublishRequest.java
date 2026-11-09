package com.psc.witkey.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskPublishRequest(
        @NotBlank(message = "任务标题不能为空")
        @Size(max = 100, message = "标题最长 100 个字符")
        String title,

        @NotNull(message = "请选择任务分类")
        Long categoryId,

        @NotBlank(message = "任务描述不能为空")
        @Size(max = 5000, message = "描述最长 5000 个字符")
        String description,

        @NotNull(message = "请填写赏金")
        @DecimalMin(value = "1.00", message = "赏金不能低于 1 元")
        BigDecimal budget,

        @NotNull(message = "请选择截止时间")
        @Future(message = "截止时间必须晚于当前时间")
        LocalDateTime deadline
) {
}
