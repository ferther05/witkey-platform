// AuthController —— 接口：注册与登录（签发 JWT）
package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.LoginRequest;
import com.psc.witkey.dto.LoginResponse;
import com.psc.witkey.dto.RegisterRequest;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口：注册与登录（签发 JWT）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 注册账号：校验用户名唯一、密码入库、发送欢迎消息
    @PostMapping("/register")
    // 注册账号
    public Result<UserVO> register(@RequestBody @Valid RegisterRequest req) {
        var vo = authService.register(req);
        // 操作留痕：注册
        com.psc.witkey.service.OpLog.of(vo.id(), "AUTH_REGISTER", "USER", vo.id(),
                "注册账号 " + vo.username() + "（" + vo.identityType() + "）");
        return Result.ok(vo);
    }

    // 登录：校验密码、签发 JWT
    @PostMapping("/login")
    // 登录签发 token
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        var resp = authService.login(req);
        // 操作留痕：登录
        com.psc.witkey.service.OpLog.of(resp.user().id(), "AUTH_LOGIN", "USER", resp.user().id(),
                "用户 " + resp.user().username() + " 登录");
        return Result.ok(resp);
    }
}
