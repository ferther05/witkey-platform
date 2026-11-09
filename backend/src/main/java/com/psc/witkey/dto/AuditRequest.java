package com.psc.witkey.dto;

import jakarta.validation.constraints.Size;

/**
 * 审核结果：通过则上架进入招募中；驳回必须写理由，雇主在"我的任务"能看到。
 */
public record AuditRequest(
        Boolean pass,
        @Size(max = 255, message = "审核意见最长 255 个字符")
        String remark
) {
}
