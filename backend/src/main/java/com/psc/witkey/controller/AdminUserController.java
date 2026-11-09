package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.service.AdminUserService;
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
 * 后台用户管理（工作项 6.1）。
 */
@RestController
@RequestMapping("/api/admin/user")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Result<PageResult<UserVO>> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminUserService.page(keyword, page, size));
    }

    @PutMapping("/{id}/status")
    public Result<UserVO> setStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = body.get("status") == null ? 0 : Integer.valueOf(body.get("status").toString());
        return Result.ok(adminUserService.setStatus(id, status));
    }
}
