// AdminResetPasswordRequest —— 请求体：管理员重置用户密码
package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理员重置密码请求。
 */
public record AdminResetPasswordRequest(
        @NotBlank(message = "请填写新密码")
        @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
        String newPassword
) {
}
