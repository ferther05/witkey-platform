package com.psc.witkey.domain;

/**
 * 任务状态机。流转规则集中在 TaskService，禁止在 Controller 里直接改状态。
 */
public enum TaskStatus {
    PENDING_AUDIT,  // 待审核
    REJECTED,       // 审核驳回
    RECRUITING,     // 招募中（大厅可见）
    SELECTED,       // 已选标（模块三）
    IN_PROGRESS,    // 进行中
    TO_ACCEPT,      // 待验收
    DONE,           // 已完成
    WITHDRAWN,      // 雇主撤回
    FLOWED;         // 到期流标

    /** 允许编辑的状态 */
    public boolean canEdit() {
        return this == PENDING_AUDIT || this == REJECTED || this == RECRUITING;
    }

    /** 是否在"进行中"之前的未成交状态 */
    public boolean isOpen() {
        return this == PENDING_AUDIT || this == REJECTED || this == RECRUITING;
    }
}
