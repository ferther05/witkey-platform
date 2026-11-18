// AdminUserController —— 后台接口：用户查询与封禁/解禁
package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.dto.AuthReviewRequest;
import com.psc.witkey.dto.AdminResetPasswordRequest;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.service.AdminUserService;
import com.psc.witkey.service.OpLog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台用户管理（工作项 6.1）：查询、封禁解禁、实名认证审核、用户详情、重置密码。
 */
@RestController
@RequestMapping("/api/admin/user")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // 分页查询用户
    @GetMapping
    // 分页查询
    public Result<PageResult<UserVO>> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminUserService.page(keyword, page, size));
    }

    // 用户详情 + 业务统计
    @GetMapping("/{id}")
    // 查询详情
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(adminUserService.detail(id));
    }

    // 封禁 / 解禁
    @PutMapping("/{id}/status")
    // 封禁/解禁
    public Result<UserVO> setStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = body.get("status") == null ? 0 : Integer.valueOf(body.get("status").toString());
        var vo = adminUserService.setStatus(id, status);
        // 操作留痕：封禁 / 解禁
        OpLog.now(status == 1 ? "USER_UNBAN" : "USER_BAN", "USER", id,
                (status == 1 ? "解禁用户 " : "封禁用户 ") + vo.username());
        return Result.ok(vo);
    }

    // 实名认证审核（通过 / 驳回）
    @PutMapping("/{id}/auth")
    // 执行 reviewAuth
    public Result<UserVO> reviewAuth(@PathVariable Long id, @RequestBody AuthReviewRequest req) {
        var vo = adminUserService.reviewAuth(id, Boolean.TRUE.equals(req.pass()), req.remark());
        // 操作留痕：实名认证审核
        OpLog.now(Boolean.TRUE.equals(req.pass()) ? "USER_AUTH_PASS" : "USER_AUTH_REJECT", "USER", id,
                (Boolean.TRUE.equals(req.pass()) ? "实名认证通过：" : "实名认证驳回：") + vo.username()
                        + (req.remark() == null || req.remark().isBlank() ? "" : "，原因：" + req.remark()));
        return Result.ok(vo);
    }

    // 管理员重置密码
    @PutMapping("/{id}/password")
    // 执行 resetPassword
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody AdminResetPasswordRequest req) {
        adminUserService.resetPassword(id, req.newPassword());
        // 操作留痕：重置密码
        OpLog.now("USER_RESET_PWD", "USER", id, "管理员重置用户 #" + id + " 的登录密码");
        return Result.ok();
    }
}
