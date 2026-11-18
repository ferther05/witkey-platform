// AdminModerationController —— 后台接口：举报列表与处理、数据看板、超时自动验收手动触发
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
    private final com.psc.witkey.service.OrderService orderService;

    @org.springframework.beans.factory.annotation.Value("${witkey.order.auto-accept-days:7}")
    private int autoAcceptDays;

    public AdminModerationController(ReportService reportService, StatsService statsService,
                                     com.psc.witkey.service.OrderService orderService) {
        this.reportService = reportService;
        this.statsService = statsService;
        this.orderService = orderService;
    }

    // 分页查询举报工单
    @GetMapping("/report")
    // 执行 reports
    public Result<PageResult<ReportVO>> reports(@RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reportService.page(status, page, size));
    }

    // 处理举报工单并按动作产生副作用
    @PostMapping("/report/{id}/handle")
    // 处理举报
    public Result<Void> handle(@PathVariable Long id, @RequestBody @Valid HandleReportRequest req) {
        reportService.handle(id, req.action(), req.remark());
        // 操作留痕：处理举报
        com.psc.witkey.service.OpLog.now("REPORT_HANDLE", "REPORT", id, "处理举报 #" + id + "，动作 " + req.action() + (req.remark() == null || req.remark().isBlank() ? "" : "，说明：" + req.remark()));
        return Result.ok();
    }

    /** 手动触发一次超时自动验收扫描（演示与测试用） */
    @org.springframework.web.bind.annotation.PostMapping("/order/auto-accept/run")
    // 手动触发自动验收
    public com.psc.witkey.common.Result<java.util.Map<String, Object>> runAutoAccept() {
        int n = orderService.autoAcceptExpired(autoAcceptDays);
        // 操作留痕：手动触发超时自动验收
        com.psc.witkey.service.OpLog.now("ORDER_AUTO_ACCEPT", "ORDER", null, "手动触发超时自动验收，处理 " + n + " 单（阈值 " + autoAcceptDays + " 天）");
        return com.psc.witkey.common.Result.ok(java.util.Map.of("handled", n, "days", autoAcceptDays));
    }

    // 统计后台看板数据
    @GetMapping("/stats/dashboard")
    // 看板统计
    public Result<DashboardVO> dashboard() {
        return Result.ok(statsService.dashboard());
    }
}
