package com.psc.witkey.security;

import com.psc.witkey.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 取当前登录用户的工具。所有涉及"我"的查询必须走这里，禁止从请求参数传 userId。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser user)) {
            throw new BusinessException(401, "未登录");
        }
        return user;
    }

    /** 允许游客访问的接口用这个：未登录返回 null，不抛异常。 */
    public static LoginUser currentOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser user)) {
            return null;
        }
        return user;
    }

    public static Long userId() {
        return current().id();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(current().role());
    }
}
