package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.domain.AuthStatus;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.domain.UserRole;
import com.psc.witkey.dto.LoginRequest;
import com.psc.witkey.dto.LoginResponse;
import com.psc.witkey.dto.RegisterRequest;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 注册与登录。注册只开放雇主/威客两种身份，管理员由系统初始化产生。
 */
@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NoticeService noticeService;

    public AuthService(SysUserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, NoticeService noticeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.noticeService = noticeService;
    }

    @Transactional
    public UserVO register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("用户名已被占用");
        }
        SysUser user = new SysUser();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(req.nickname() == null || req.nickname().isBlank()
                ? req.username() : req.nickname().trim());
        user.setRole(UserRole.valueOf(req.identityType()).name());
        user.setIdentityType(req.identityType());
        user.setAuthStatus(AuthStatus.UNVERIFIED.name());
        user.setStatus(1);
        SysUser saved = userRepository.save(user);

        // 注册即送一条欢迎消息，顺便让消息模块一上线就有内容
        noticeService.push(saved.getId(), "SYSTEM", "欢迎加入威客帮",
                "完善个人资料并完成实名认证后，就可以发布或承接任务了。", "USER", saved.getId());

        return UserVO.from(saved);
    }

    public LoginResponse login(LoginRequest req) {
        SysUser user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        if (user.isBanned()) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, UserVO.from(user));
    }
}
