// LoginUser —— 登录用户模型：实现 UserDetails，供 Spring
package com.psc.witkey.security;

/**
 * 登录态主体：只放鉴权必需的信息，避免每次请求都查库。
 */
public record LoginUser(Long id, String username, String role) {
}
