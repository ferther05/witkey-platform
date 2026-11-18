// IdentityTypeRequest —— 请求体：切换身份（雇主/威客）
package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 请求体：切换身份（雇主/威客）。
 */
public record IdentityTypeRequest(
        @NotBlank(message = "请选择身份")
        @Pattern(regexp = "EMPLOYER|WITKEY", message = "身份只能是 EMPLOYER 或 WITKEY")
        String identityType
) {
}
