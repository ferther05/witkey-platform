// RejectRequest —— 请求体：验收驳回理由
package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 请求体：验收驳回理由。
 */
public record RejectRequest(
        @NotBlank(message = "请填写驳回原因")
        @Size(max = 500, message = "驳回原因最长 500 个字符")
        String reason
) {
}
