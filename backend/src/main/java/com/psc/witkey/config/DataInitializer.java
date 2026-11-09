package com.psc.witkey.config;

import com.psc.witkey.domain.AuthStatus;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.domain.UserRole;
import com.psc.witkey.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动时创建管理员账号（admin / 123456），已存在则不处理。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNickname("平台管理员");
        admin.setRole(UserRole.ADMIN.name());
        admin.setIdentityType("EMPLOYER");
        admin.setAuthStatus(AuthStatus.VERIFIED.name());
        admin.setStatus(1);
        userRepository.save(admin);
        log.info("已初始化管理员账号：admin / 123456");
    }
}
