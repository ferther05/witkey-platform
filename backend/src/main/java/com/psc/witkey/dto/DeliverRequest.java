package com.psc.witkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DeliverRequest(
        @NotBlank(message = "请填写交付说明")
        @Size(max = 1000, message = "交付说明最长 1000 个字符")
        String note,

        /** 交付附件 id 列表（可空） */
        List<Long> fileIds
) {
}
