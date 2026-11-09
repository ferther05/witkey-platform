package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.dto.TaskPublishRequest;
import com.psc.witkey.dto.TaskVO;
import com.psc.witkey.security.LoginUser;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** 任务大厅（游客可看） */
    @GetMapping
    public Result<PageResult<TaskVO>> hall(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) BigDecimal minBudget,
                                           @RequestParam(required = false) BigDecimal maxBudget,
                                           @RequestParam(defaultValue = "latest") String sort,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "12") int size) {
        LoginUser viewer = SecurityUtils.currentOrNull();
        return Result.ok(taskService.hall(keyword, categoryId, minBudget, maxBudget, sort,
                viewer == null ? null : viewer.id(), page, size));
    }

    /** 任务详情（游客可看，未上架任务仅雇主本人/管理员可见） */
    @GetMapping("/{id}")
    public Result<TaskVO> detail(@PathVariable Long id) {
        LoginUser viewer = SecurityUtils.currentOrNull();
        return Result.ok(taskService.detail(id,
                viewer == null ? null : viewer.id(),
                viewer != null && "ADMIN".equals(viewer.role())));
    }

    /** 我发布的任务 */
    @GetMapping("/my")
    public Result<PageResult<TaskVO>> myTasks(@RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(taskService.myTasks(SecurityUtils.userId(), status, page, size));
    }

    /** 我的收藏 */
    @GetMapping("/favorite")
    public Result<PageResult<TaskVO>> myFavorites(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.ok(taskService.myFavorites(SecurityUtils.userId(), page, size));
    }

    /** 收藏 / 取消收藏，返回最新收藏状态 */
    @PostMapping("/{id}/favorite")
    public Result<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        boolean favorite = taskService.toggleFavorite(SecurityUtils.userId(), id);
        return Result.ok(Map.of("favorite", favorite));
    }

    @PostMapping
    public Result<TaskVO> publish(@RequestBody @Valid TaskPublishRequest req) {
        return Result.ok(taskService.publish(SecurityUtils.userId(), req));
    }

    @PutMapping("/{id}")
    public Result<TaskVO> update(@PathVariable Long id, @RequestBody @Valid TaskPublishRequest req) {
        return Result.ok(taskService.update(SecurityUtils.userId(), id, req));
    }

    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        taskService.withdraw(SecurityUtils.userId(), id);
        return Result.ok();
    }
}
