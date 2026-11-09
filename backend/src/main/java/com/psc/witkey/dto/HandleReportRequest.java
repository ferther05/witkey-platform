package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 举报处理：action 决定副作用
 * REJECT 驳回 / TAKE_DOWN 下架任务 / BAN 封禁用户 / REFUND 订单强制退款 / PAY 订单强制打款
 */
public record HandleReportRequest(
        @NotBlank(message = "请选择处理方式")
        @Pattern(regexp = "REJECT|TAKE_DOWN|BAN|REFUND|PAY", message = "处理方式不合法")
        String action,

        @Size(max = 500, message = "处理说明最长 500 个字符")
        String remark
) {
}
