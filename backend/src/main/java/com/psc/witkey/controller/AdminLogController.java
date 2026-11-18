// AdminLogController —— 接口：后台操作日志查询
package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.dto.OperationLogVO;
import com.psc.witkey.service.OperationLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 后台接口：操作日志（所有人操作留痕）。仅管理员可访问。
 */
@RestController
@RequestMapping("/api/admin/log")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    private final OperationLogService logService;

    public AdminLogController(OperationLogService logService) {
        this.logService = logService;
    }

    @GetMapping
    // 分页查询
    public Result<PageResult<OperationLogVO>> page(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(logService.page(action, keyword, userId, start, end, page, size));
    }

    /** 动作字典，供前端筛选下拉使用 */
    @GetMapping("/actions")
    // 动作字典
    public Result<Map<String, String>> actions() {
        return Result.ok(OperationLogService.ACTIONS);
    }
}
