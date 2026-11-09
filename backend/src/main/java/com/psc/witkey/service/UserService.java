package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.domain.AuthStatus;
import com.psc.witkey.domain.IdentityType;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.domain.UserRole;
import com.psc.witkey.dto.ChangePasswordRequest;
import com.psc.witkey.dto.IdentityApplyRequest;
import com.psc.witkey.dto.UpdateProfileRequest;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.repository.SysUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 个人资料、密码、身份认证与身份切换。所有操作只作用于当前登录用户。
 */
@Service
public class UserService {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SysUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    public UserVO me(Long userId) {
        return UserVO.from(getById(userId));
    }

    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest req) {
        SysUser user = getById(userId);
        if (req.nickname() != null && !req.nickname().isBlank()) {
            user.setNickname(req.nickname().trim());
        }
        user.setAvatar(req.avatar());
        user.setIntro(req.intro());
        user.setSkills(req.skills());
        if (req.mobile() != null && !req.mobile().isBlank()) {
            user.setMobile(req.mobile());
        }
        return UserVO.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        SysUser user = getById(userId);
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (req.oldPassword().equals(req.newPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    /**
     * 提交实名认证申请：未认证或已驳回状态下可提交，提交后进入待审核。
     */
    @Transactional
    public UserVO applyIdentity(Long userId, IdentityApplyRequest req) {
        SysUser user = getById(userId);
        String current = user.getAuthStatus();
        if (AuthStatus.PENDING.name().equals(current)) {
            throw new BusinessException("认证申请审核中，请耐心等待");
        }
        if (AuthStatus.VERIFIED.name().equals(current)) {
            throw new BusinessException("已完成实名认证，无需重复提交");
        }
        user.setRealName(req.realName().trim());
        user.setMobile(req.mobile());
        user.setAuthStatus(AuthStatus.PENDING.name());
        return UserVO.from(userRepository.save(user));
    }

    /**
     * 切换主身份（雇主 ⇄ 威客）。权限角色同步变更，避免出现"雇主发任务却无雇主权限"。
     */
    @Transactional
    public UserVO switchIdentityType(Long userId, String identityType) {
        SysUser user = getById(userId);
        if (UserRole.ADMIN.name().equals(user.getRole())) {
            throw new BusinessException("管理员不支持切换身份");
        }
        IdentityType target;
        try {
            target = IdentityType.valueOf(identityType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("身份类型不合法");
        }
        user.setIdentityType(target.name());
        user.setRole(target.name());
        return UserVO.from(userRepository.save(user));
    }
}
