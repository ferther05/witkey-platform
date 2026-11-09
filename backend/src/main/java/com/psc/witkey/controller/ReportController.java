package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.CreateReportRequest;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 前台举报：举报任务 / 用户 / 订单 */
    @PostMapping
    public Result<Void> submit(@RequestBody @Valid CreateReportRequest req) {
        reportService.submit(SecurityUtils.userId(), req);
        return Result.ok();
    }
}
