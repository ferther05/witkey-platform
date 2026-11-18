// DeliverRequest —— 请求体：交付成果（说明 + 附件 id 列表）
package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 请求体：交付成果（说明 + 附件 id 列表）。
 */
public record DeliverRequest(
        @NotBlank(message = "请填写交付说明")
        @Size(max = 1000, message = "交付说明最长 1000 个字符")
        String note,

        /** 交付附件 id 列表（可空） */
        List<Long> fileIds
) {
}
