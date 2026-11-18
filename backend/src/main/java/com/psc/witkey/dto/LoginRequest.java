// LoginRequest —— 请求体：登录（用户名 + 密码）
package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 请求体：登录（用户名 + 密码）。
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password
) {
}
