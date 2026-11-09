package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateReportRequest(
        @NotBlank(message = "请选择举报对象类型")
        @Pattern(regexp = "TASK|USER|ORDER", message = "举报对象类型不合法")
        String targetType,

        @NotNull(message = "缺少举报对象")
        Long targetId,

        @NotBlank(message = "请填写举报理由")
        @Size(max = 500, message = "举报理由最长 500 个字符")
        String reason,

        List<Long> fileIds
) {
}
