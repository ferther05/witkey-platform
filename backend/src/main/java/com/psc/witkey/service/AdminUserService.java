package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台用户管理：搜索、封禁/解封。封禁后无法登录、无法投标（登录时校验）。
 */
@Service
public class AdminUserService {

    private final SysUserRepository userRepository;

    public AdminUserService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResult<UserVO> page(String keyword, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(p - 1, s);
        Page<SysUser> result = StringUtils.hasText(keyword)
                ? userRepository.findByUsernameContainingOrNicknameContainingOrderByCreatedAtDesc(
                keyword.trim(), keyword.trim(), pageable)
                : userRepository.findAllByOrderByCreatedAtDesc(pageable);
        return new PageResult<>(result.getContent().stream().map(UserVO::from).toList(),
                result.getTotalElements(), p, s);
    }

    @Transactional
    public UserVO setStatus(Long userId, Integer status) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能封禁管理员账号");
        }
        user.setStatus(status == null || status == 0 ? 0 : 1);
        return UserVO.from(userRepository.save(user));
    }
}
