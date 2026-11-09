package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IdentityApplyRequest(
        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 50, message = "姓名过长")
        String realName,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String mobile
) {
}
