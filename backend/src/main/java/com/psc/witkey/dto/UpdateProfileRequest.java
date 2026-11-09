package com.psc.witkey.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50, message = "昵称最长 50 个字符") String nickname,
        @Size(max = 255, message = "头像地址过长") String avatar,
        @Size(max = 255, message = "简介最长 255 个字符") String intro,
        @Size(max = 255, message = "技能标签过长") String skills,
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确") String mobile
) {
}
