// AdminTaskController —— 后台接口：待审核队列与审核通过/驳回
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

    // 分页查询待审核任务（最新提交在前）
    @GetMapping("/audit")
    // 查询待审核队列
    public Result<PageResult<TaskVO>> pendingAudit(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.ok(taskService.pendingAudit(page, size));
    }

    // 审核任务：通过进入招募中，驳回写入意见并通知雇主
    @PostMapping("/{id}/audit")
    // 审核任务
    public Result<TaskVO> audit(@PathVariable Long id, @RequestBody AuditRequest req) {
        var vo = taskService.audit(id, Boolean.TRUE.equals(req.pass()), req.remark());
        // 操作留痕：审核任务
        com.psc.witkey.service.OpLog.now("TASK_AUDIT", "TASK", id,
                (Boolean.TRUE.equals(req.pass()) ? "审核通过" : "审核驳回") + "《" + vo.title() + "》"
                        + (req.remark() == null || req.remark().isBlank() ? "" : "，意见：" + req.remark()));
        return Result.ok(vo);
    }
}
