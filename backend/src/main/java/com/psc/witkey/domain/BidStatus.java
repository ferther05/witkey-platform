package com.psc.witkey.domain;

/**
 * 投标状态：待选 -> 中标 / 落选，或威客自己撤回。
 */
public enum BidStatus {
    PENDING,    // 待选
    WIN,        // 中标
    LOSE,       // 未中标
    WITHDRAWN   // 已撤回
}
