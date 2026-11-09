package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名需为 4-20 位字母、数字或下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
        String password,

        @Size(max = 50, message = "昵称最长 50 个字符")
        String nickname,

        @NotBlank(message = "请选择身份")
        @Pattern(regexp = "EMPLOYER|WITKEY", message = "身份只能是 EMPLOYER 或 WITKEY")
        String identityType
) {
}
