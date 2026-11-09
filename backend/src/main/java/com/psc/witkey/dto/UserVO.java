package com.psc.witkey.dto;

import com.psc.witkey.domain.SysUser;

import java.time.LocalDateTime;

/**
 * 对外暴露的用户信息，不包含密码。
 */
public record UserVO(
        Long id,
        String username,
        String nickname,
        String avatar,
        String intro,
        String skills,
        String mobile,
        String role,
        String identityType,
        String authStatus,
        String realName,
        Integer status,
        LocalDateTime createdAt
) {

    public static UserVO from(SysUser u) {
        return new UserVO(u.getId(), u.getUsername(), u.getNickname(), u.getAvatar(), u.getIntro(),
                u.getSkills(), u.getMobile(), u.getRole(), u.getIdentityType(), u.getAuthStatus(),
                u.getRealName(), u.getStatus(), u.getCreatedAt());
    }
}
