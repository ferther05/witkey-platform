package com.psc.witkey.dto;

import com.psc.witkey.domain.TaskCategory;

import java.math.BigDecimal;

public record CategoryVO(Long id, String name, BigDecimal commissionRate, Integer sort, Integer status) {

    public static CategoryVO from(TaskCategory c) {
        return new CategoryVO(c.getId(), c.getName(), c.getCommissionRate(), c.getSort(), c.getStatus());
    }
}
