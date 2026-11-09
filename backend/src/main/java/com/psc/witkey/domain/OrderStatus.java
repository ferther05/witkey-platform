package com.psc.witkey.domain;

/**
 * 订单状态：选标后生成订单 -> 交付 -> 验收 -> 结算完成。
 */
public enum OrderStatus {
    TO_DELIVER,  // 待交付
    TO_ACCEPT,   // 待验收
    DONE,        // 已完成（已结算）
    REFUNDED     // 已退款（举报/仲裁用）
}
