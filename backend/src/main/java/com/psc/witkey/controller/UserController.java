// UserController —— 接口：当前用户资料、改密、实名认证、身份切换
package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.ChangePasswordRequest;
import com.psc.witkey.dto.IdentityApplyRequest;
import com.psc.witkey.dto.IdentityTypeRequest;
import com.psc.witkey.dto.UpdateProfileRequest;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口：当前用户资料、改密、实名认证、身份切换。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 当前登录用户信息 */
    @GetMapping("/me")
    // 当前用户信息
    public Result<UserVO> me() {
        return Result.ok(userService.me(SecurityUtils.userId()));
    }

    // 更新个人资料
    @PutMapping("/profile")
    // 更新个人资料
    public Result<UserVO> updateProfile(@RequestBody @Valid UpdateProfileRequest req) {
        return Result.ok(userService.updateProfile(SecurityUtils.userId(), req));
    }

    // 修改密码
    @PutMapping("/password")
    // 修改密码
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
        userService.changePassword(SecurityUtils.userId(), req);
        return Result.ok();
    }

    /** 提交实名认证申请 */
    @PostMapping("/identity/apply")
    // 提交实名认证
    public Result<UserVO> applyIdentity(@RequestBody @Valid IdentityApplyRequest req) {
        return Result.ok(userService.applyIdentity(SecurityUtils.userId(), req));
    }

    /** 切换主身份：雇主 ⇄ 威客 */
    @PutMapping("/identity-type")
    // 切换身份
    public Result<UserVO> switchIdentityType(@RequestBody @Valid IdentityTypeRequest req) {
        return Result.ok(userService.switchIdentityType(SecurityUtils.userId(), req.identityType()));
    }
}
