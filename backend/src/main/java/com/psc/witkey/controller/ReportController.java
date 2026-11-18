// ReportController —— 接口：用户发起举报
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

/**
 * 接口：用户发起举报。
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 前台举报：举报任务 / 用户 / 订单 */
    @PostMapping
    // 提交
    public Result<Void> submit(@RequestBody @Valid CreateReportRequest req) {
        reportService.submit(SecurityUtils.userId(), req);
        com.psc.witkey.service.OpLog.now("REPORT_CREATE", "REPORT", req.targetId(), "举报 " + req.targetType() + " #" + req.targetId() + "：" + req.reason());
        return Result.ok();
    }
}
