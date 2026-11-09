package com.psc.witkey.domain;

/**
 * 实名认证状态：未认证 -> 待审核 -> 已认证/已驳回。
 */
public enum AuthStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED
}
