// OperationLogService —— 业务：操作日志记录与查询
package com.psc.witkey.service;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.OperationLog;
import com.psc.witkey.dto.OperationLogVO;
import com.psc.witkey.repository.OperationLogRepository;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.security.LoginUser;
import com.psc.witkey.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 业务：操作日志（审计留痕）。记录所有关键动作，并提供管理员查看用的分页查询。
 */
@Service
public class OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogService.class);

    /** 动作编码 -> 中文说明（前端也用这份字典展示） */
    public static final Map<String, String> ACTIONS = new java.util.LinkedHashMap<>() {{
        put("AUTH_REGISTER", "注册账号");
        put("AUTH_LOGIN", "登录");
        put("TASK_PUBLISH", "发布任务");
        put("TASK_EDIT", "编辑任务");
        put("TASK_WITHDRAW", "撤回任务");
        put("TASK_AUDIT", "审核任务");
        put("TASK_FLOW", "任务流标");
        put("BID_CREATE", "提交投标");
        put("BID_UPDATE", "修改投标");
        put("BID_WITHDRAW", "撤回投标");
        put("BID_SELECT", "选标中标");
        put("ORDER_DELIVER", "交付成果");
        put("ORDER_ACCEPT", "验收通过");
        put("ORDER_REJECT", "驳回验收");
        put("ORDER_AUTO_ACCEPT", "超时自动验收");
        put("ORDER_REFUND", "强制退款");
        put("ORDER_PAY", "强制打款");
        put("WALLET_RECHARGE", "模拟充值");
        put("WALLET_WITHDRAW", "模拟提现");
        put("USER_BAN", "封禁用户");
        put("USER_UNBAN", "解禁用户");
        put("REPORT_CREATE", "发起举报");
        put("REPORT_HANDLE", "处理举报");
        put("CATEGORY_CREATE", "新增分类");
        put("CATEGORY_UPDATE", "修改分类");
        put("CATEGORY_DELETE", "删除分类");
        put("FILE_UPLOAD", "上传文件");
        put("FILE_DELETE", "删除文件");
    }};

    private final OperationLogRepository logRepository;
    private final SysUserRepository userRepository;

    public OperationLogService(OperationLogRepository logRepository, SysUserRepository userRepository) {
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    /**
     * 记录一条操作日志。写日志失败不影响主流程（审计不能反向拖垮业务）。
     */
    public void record(Long userId, String action, String targetType, Long targetId, String detail) {
        try {
            OperationLog entity = new OperationLog();
            entity.setUserId(userId);
            if (userId != null) {
                userRepository.findById(userId).ifPresent(u -> {
                    entity.setUsername(u.getUsername());
                    entity.setRole(u.getRole());
                });
            }
            entity.setAction(action);
            entity.setTargetType(targetType);
            entity.setTargetId(targetId);
            entity.setDetail(detail == null ? null : detail.substring(0, Math.min(detail.length(), 480)));
            logRepository.save(entity);
        } catch (Exception e) {
            log.warn("写操作日志失败: action={}, err={}", action, e.getMessage());
        }
    }

    /** 以当前登录人身份记录（控制器里一行搞定） */
    public void recordCurrent(String action, String targetType, Long targetId, String detail) {
        LoginUser user = SecurityUtils.currentOrNull();
        record(user == null ? null : user.id(), action, targetType, targetId, detail);
    }

    /** 管理员查看：支持按动作、用户、关键词、时间范围筛选 */
    public PageResult<OperationLogVO> page(String action, String keyword, Long userId,
                                           LocalDateTime start, LocalDateTime end,
                                           int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);

        Specification<OperationLog> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (StringUtils.hasText(action)) {
                ps.add(cb.equal(root.get("action"), action.trim().toUpperCase()));
            }
            if (userId != null) {
                ps.add(cb.equal(root.get("userId"), userId));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                ps.add(cb.or(cb.like(root.get("username"), like),
                        cb.like(root.get("detail"), like),
                        cb.like(root.get("action"), like)));
            }
            if (start != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<OperationLog> result = logRepository.findAll(spec,
                PageRequest.of(p - 1, s, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<OperationLogVO> list = result.getContent().stream().map(e -> new OperationLogVO(
                        e.getId(), e.getUserId(), e.getUsername(), e.getRole(),
                        e.getAction(), ACTIONS.getOrDefault(e.getAction(), e.getAction()),
                        e.getTargetType(), e.getTargetId(), e.getDetail(), e.getCreatedAt()))
                .toList();
        return new PageResult<>(list, result.getTotalElements(), p, s);
    }
}
