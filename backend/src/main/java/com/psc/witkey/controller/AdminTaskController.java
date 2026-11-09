package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.dto.AuditRequest;
import com.psc.witkey.dto.TaskVO;
import com.psc.witkey.service.TaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台任务审核（工作项 2.3 / 6.2）。
 */
@RestController
@RequestMapping("/api/admin/task")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTaskController {

    private final TaskService taskService;

    public AdminTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/audit")
    public Result<PageResult<TaskVO>> pendingAudit(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.ok(taskService.pendingAudit(page, size));
    }

    @PostMapping("/{id}/audit")
    public Result<TaskVO> audit(@PathVariable Long id, @RequestBody AuditRequest req) {
        return Result.ok(taskService.audit(id, Boolean.TRUE.equals(req.pass()), req.remark()));
    }
}
