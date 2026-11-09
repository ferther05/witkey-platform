package com.psc.witkey.domain;

/**
 * 角色（权限维度）。与"身份"不同：一个雇主也可以是威客，但权限主体按角色划分。
 */
public enum UserRole {
    EMPLOYER,   // 雇主
    WITKEY,     // 威客
    ADMIN       // 管理员
}
