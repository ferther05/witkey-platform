package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.dto.DashboardVO;
import com.psc.witkey.dto.HandleReportRequest;
import com.psc.witkey.dto.ReportVO;
import com.psc.witkey.service.ReportService;
import com.psc.witkey.service.StatsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报处理（6.3）与数据看板（6.5）。
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    private final ReportService reportService;
    private final StatsService statsService;

    public AdminModerationController(ReportService reportService, StatsService statsService) {
        this.reportService = reportService;
        this.statsService = statsService;
    }

    @GetMapping("/report")
    public Result<PageResult<ReportVO>> reports(@RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reportService.page(status, page, size));
    }

    @PostMapping("/report/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody @Valid HandleReportRequest req) {
        reportService.handle(id, req.action(), req.remark());
        return Result.ok();
    }

    @GetMapping("/stats/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(statsService.dashboard());
    }
}
