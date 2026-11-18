// LoginResponse —— 响应体：登录成功（token + 用户信息）
package com.psc.witkey.dto;

/**
 * 响应体：登录成功（token + 用户信息）。
 */
public record LoginResponse(String token, UserVO user) {
}
