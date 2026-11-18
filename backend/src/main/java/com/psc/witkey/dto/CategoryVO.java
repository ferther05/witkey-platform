// CategoryVO —— 响应体：任务分类
package com.psc.witkey.dto;

import com.psc.witkey.domain.TaskCategory;

import java.math.BigDecimal;

/**
 * 响应体：任务分类。
 */
public record CategoryVO(Long id, String name, BigDecimal commissionRate, Integer sort, Integer status) {

    // 执行 from
    public static CategoryVO from(TaskCategory c) {
        return new CategoryVO(c.getId(), c.getName(), c.getCommissionRate(), c.getSort(), c.getStatus());
    }
}
