// ChangePasswordRequest —— 请求体：修改密码（原密码 + 新密码）
package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 请求体：修改密码（原密码 + 新密码）。
 */
public record ChangePasswordRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 32, message = "新密码长度需为 6-32 位")
        String newPassword
) {
}
