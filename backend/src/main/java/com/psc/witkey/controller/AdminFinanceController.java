package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.FinanceSummaryVO;
import com.psc.witkey.service.FinanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFinanceController {

    private final FinanceService financeService;

    public AdminFinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/summary")
    public Result<FinanceSummaryVO> summary() {
        return Result.ok(financeService.summary());
    }
}
