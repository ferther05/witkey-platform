// AuthReviewRequest —— 请求体：实名认证审核（是否通过 + 原因）
package com.psc.witkey.dto;

import jakarta.validation.constraints.Size;

/**
 * 实名认证审核请求：pass=true 通过；pass=false 必须带原因。
 */
public record AuthReviewRequest(
        Boolean pass,
        @Size(max = 255, message = "审核意见最长 255 个字符")
        String remark
) {
}
