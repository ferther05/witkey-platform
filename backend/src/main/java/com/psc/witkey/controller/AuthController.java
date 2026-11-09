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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return Result.ok(authService.login(req));
    }
}
