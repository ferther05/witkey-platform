// TaskController —— 接口：任务大厅、详情、我的任务、发布、编辑、撤回、收藏
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

/**
 * 接口：任务大厅、详情、我的任务、发布、编辑、撤回、收藏。
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** 任务大厅（游客可看） */
    @GetMapping
    // 任务大厅查询
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
    // 查询详情
    public Result<TaskVO> detail(@PathVariable Long id) {
        LoginUser viewer = SecurityUtils.currentOrNull();
        return Result.ok(taskService.detail(id,
                viewer == null ? null : viewer.id(),
                viewer != null && "ADMIN".equals(viewer.role())));
    }

    /** 我发布的任务 */
    @GetMapping("/my")
    // 我的任务
    public Result<PageResult<TaskVO>> myTasks(@RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(taskService.myTasks(SecurityUtils.userId(), status, page, size));
    }

    /** 我的收藏 */
    @GetMapping("/favorite")
    // 我的收藏
    public Result<PageResult<TaskVO>> myFavorites(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.ok(taskService.myFavorites(SecurityUtils.userId(), page, size));
    }

    /** 收藏 / 取消收藏，返回最新收藏状态 */
    @PostMapping("/{id}/favorite")
    // 切换收藏
    public Result<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        boolean favorite = taskService.toggleFavorite(SecurityUtils.userId(), id);
        return Result.ok(Map.of("favorite", favorite));
    }

    // 发布任务：校验参数、冻结赏金、置为待审核
    @PostMapping
    // 发布任务并冻结赏金
    public Result<TaskVO> publish(@RequestBody @Valid TaskPublishRequest req) {
        var vo = taskService.publish(SecurityUtils.userId(), req);
        // 操作留痕：发布任务
        com.psc.witkey.service.OpLog.now("TASK_PUBLISH", "TASK", vo.id(),
                "发布任务《" + vo.title() + "》赏金 " + vo.budget());
        return Result.ok(vo);
    }

    // 更新任务：重新回到待审核并调整托管金额
    @PutMapping("/{id}")
    // 更新数据
    public Result<TaskVO> update(@PathVariable Long id, @RequestBody @Valid TaskPublishRequest req) {
        var vo = taskService.update(SecurityUtils.userId(), id, req);
        // 操作留痕：编辑任务
        com.psc.witkey.service.OpLog.now("TASK_EDIT", "TASK", id,
                "编辑任务《" + vo.title() + "》赏金 " + vo.budget() + "，重新进入待审核");
        return Result.ok(vo);
    }

    // 撤回：任务撤回 / 提现，按类语义执行
    @PostMapping("/{id}/withdraw")
    // 撤回 / 提现
    public Result<Void> withdraw(@PathVariable Long id) {
        taskService.withdraw(SecurityUtils.userId(), id);
        // 操作留痕：撤回任务
        com.psc.witkey.service.OpLog.now("TASK_WITHDRAW", "TASK", id, "撤回任务 #" + id + "，赏金解冻");
        return Result.ok();
    }
}
