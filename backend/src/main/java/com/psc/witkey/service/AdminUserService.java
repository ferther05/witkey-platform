// AdminUserService —— 业务：后台用户查询与封禁解禁
package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.AuthStatus;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.dto.UserVO;
import com.psc.witkey.repository.BidRepository;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.repository.TaskRepository;
import com.psc.witkey.repository.WalletAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 后台用户管理：搜索、封禁/解封、实名认证审核、用户详情统计、重置密码。
 * 封禁后无法登录与调用接口（JWT 过滤器每次请求校验账号状态）。
 */
@Service
public class AdminUserService {

    private final SysUserRepository userRepository;
    private final TaskRepository taskRepository;
    private final BizOrderRepository orderRepository;
    private final BidRepository bidRepository;
    private final WalletAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final NoticeService noticeService;

    public AdminUserService(SysUserRepository userRepository,
                            TaskRepository taskRepository,
                            BizOrderRepository orderRepository,
                            BidRepository bidRepository,
                            WalletAccountRepository accountRepository,
                            PasswordEncoder passwordEncoder,
                            NoticeService noticeService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.orderRepository = orderRepository;
        this.bidRepository = bidRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.noticeService = noticeService;
    }

    // 分页查询用户（用户名/昵称模糊搜索）
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

    // 封禁 / 解禁（管理员账号不可封禁）
    @Transactional
    // 封禁/解禁
    public UserVO setStatus(Long userId, Integer status) {
        SysUser user = requireUser(userId);
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能封禁管理员账号");
        }
        user.setStatus(status == null || status == 0 ? 0 : 1);
        return UserVO.from(userRepository.save(user));
    }

    /**
     * 实名认证审核：只有「审核中」的用户可审。
     * 通过 → VERIFIED；驳回 → REJECTED + 原因（用户个人中心可见），并推站内消息。
     */
    @Transactional
    // 执行 reviewAuth
    public UserVO reviewAuth(Long userId, boolean pass, String remark) {
        SysUser user = requireUser(userId);
        if (!AuthStatus.PENDING.name().equals(user.getAuthStatus())) {
            throw new BusinessException("该用户当前没有待审核的实名认证");
        }
        if (!pass && (remark == null || remark.isBlank())) {
            throw new BusinessException("驳回需要填写原因");
        }
        if (pass) {
            user.setAuthStatus(AuthStatus.VERIFIED.name());
            user.setAuthRemark(remark == null || remark.isBlank() ? null : remark.trim());
        } else {
            user.setAuthStatus(AuthStatus.REJECTED.name());
            user.setAuthRemark(remark.trim());
        }
        SysUser saved = userRepository.save(user);

        noticeService.push(user.getId(), "SYSTEM", pass ? "实名认证已通过" : "实名认证未通过",
                pass ? "你的实名认证已通过，可以使用全部功能。"
                        : "你的实名认证未通过：" + remark.trim() + "，修改后可重新提交。",
                "USER", user.getId());
        return UserVO.from(saved);
    }

    /** 用户详情：基础资料 + 任务/订单/钱包统计，供后台详情抽屉展示 */
    public Map<String, Object> detail(Long userId) {
        SysUser user = requireUser(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", UserVO.from(user));
        data.put("employerTaskCount", taskRepository.countByEmployerId(userId));
        data.put("employerOrderCount", orderRepository.countByEmployerId(userId));
        data.put("witkeyOrderCount", orderRepository.countByWitkeyId(userId));
        data.put("doneAsEmployer", orderRepository.countByEmployerIdAndStatus(userId, "DONE"));
        data.put("doneAsWitkey", orderRepository.countByWitkeyIdAndStatus(userId, "DONE"));
        data.put("bidCount", bidRepository.countByWitkeyId(userId));
        BigDecimal balance = accountRepository.findByUserId(userId)
                .map(a -> a.getBalance()).orElse(BigDecimal.ZERO);
        BigDecimal frozen = accountRepository.findByUserId(userId)
                .map(a -> a.getFrozen()).orElse(BigDecimal.ZERO);
        data.put("balance", balance);
        data.put("frozen", frozen);
        return data;
    }

    /** 管理员重置密码：直接设置新密码（6-32 位），并通知本人 */
    @Transactional
    // 执行 resetPassword
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = requireUser(userId);
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 32) {
            throw new BusinessException("密码长度需为 6-32 位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        noticeService.push(user.getId(), "SYSTEM", "密码已被重置",
                "管理员已重置你的登录密码，如非本人操作请联系平台。", "USER", user.getId());
    }

    // 取用户，不存在则抛 404
    private SysUser requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
}
