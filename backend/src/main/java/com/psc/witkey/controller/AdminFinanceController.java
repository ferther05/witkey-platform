// AdminFinanceController —— 后台接口：平台资金汇总（成交额、佣金、托管）
package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.FinanceSummaryVO;
import com.psc.witkey.service.FinanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台接口：平台资金汇总（成交额、佣金、托管）。
 */
@RestController
@RequestMapping("/api/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFinanceController {

    private final FinanceService financeService;

    public AdminFinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    // 汇总统计
    // 平台资金流水（可按类型/用户/关键词/时间筛选）
    @GetMapping("/flows")
    // 执行 flows
    public Result<com.psc.witkey.common.PageResult<com.psc.witkey.dto.WalletFlowVO>> flows(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime start,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(financeService.flows(type, userId, keyword, start, end, page, size));
    }

    // 导出 CSV（浏览器直接下载）
    @GetMapping("/flows/export")
    // 执行 export
    public org.springframework.http.ResponseEntity<byte[]> export(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime start,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime end) {
        String csv = financeService.exportCsv(type, userId, keyword, start, end);
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=wallet_flows.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/summary")
    // 资金汇总
    public Result<FinanceSummaryVO> summary() {
        return Result.ok(financeService.summary());
    }
}
