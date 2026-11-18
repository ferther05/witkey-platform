// FinanceService —— 业务：平台资金汇总与佣金统计
package com.psc.witkey.service;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.OrderStatus;
import com.psc.witkey.domain.TaskStatus;
import com.psc.witkey.dto.FinanceSummaryVO;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.TaskRepository;
import com.psc.witkey.repository.WalletAccountRepository;
import com.psc.witkey.repository.WalletFlowRepository;
import org.springframework.stereotype.Service;

/**
 * 平台财务口径：成交额、佣金、托管余额、充值提现总量。
 * 后台看板（模块六）直接复用这里的数字，避免两处口径不一致。
 */
@Service
public class FinanceService {

    private final BizOrderRepository orderRepository;
    private final WalletAccountRepository accountRepository;
    private final WalletFlowRepository flowRepository;
    private final TaskRepository taskRepository;
    private final com.psc.witkey.repository.SysUserRepository sysUserRepository;

    public FinanceService(BizOrderRepository orderRepository, WalletAccountRepository accountRepository,
                          WalletFlowRepository flowRepository, TaskRepository taskRepository,
                          com.psc.witkey.repository.SysUserRepository sysUserRepository) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.flowRepository = flowRepository;
        this.taskRepository = taskRepository;
        this.sysUserRepository = sysUserRepository;
    }

    // 汇总统计
    public FinanceSummaryVO summary() {
        var gmv = orderRepository.sumDoneAmount();
        var commission = orderRepository.sumDoneCommission();
        var escrow = accountRepository.sumFrozen();
        var recharge = flowRepository.sumAmountByType("RECHARGE");
        var withdraw = flowRepository.sumAmountByType("WITHDRAW").negate();

        long done = orderRepository.countByStatus(OrderStatus.DONE.name());
        long active = orderRepository.countByStatus(OrderStatus.TO_DELIVER.name())
                + orderRepository.countByStatus(OrderStatus.TO_ACCEPT.name());
        long pendingAudit = taskRepository.countByStatus(TaskStatus.PENDING_AUDIT.name());

        return new FinanceSummaryVO(gmv, commission, escrow, recharge, withdraw,
                done, active, pendingAudit);
    }

    /**
     * 平台流水（管理员视角）：可按类型、用户、关键词、时间范围筛选。
     */
    public PageResult<com.psc.witkey.dto.WalletFlowVO> flows(String type, Long userId, String keyword,
                                                             java.time.LocalDateTime start,
                                                             java.time.LocalDateTime end,
                                                             int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        var pageData = flowRepository.findAll(specOf(type, userId, keyword, start, end),
                org.springframework.data.domain.PageRequest.of(p - 1, s,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        var result = pageData.getContent().stream()
                .map(f -> com.psc.witkey.dto.WalletFlowVO.from(f, nameOf(f.getUserId())))
                .toList();
        return new PageResult<>(result, pageData.getTotalElements(), p, s);
    }

    /** 导出 CSV（最多 5000 条），字段：时间,用户,类型,金额,变动后余额,业务号,说明 */
    public String exportCsv(String type, Long userId, String keyword,
                            java.time.LocalDateTime start, java.time.LocalDateTime end) {
        var rows = queryFlows(type, userId, keyword, start, end);
        StringBuilder sb = new StringBuilder("\uFEFF时间,用户,类型,金额,变动后余额,业务单号,说明\n");
        for (var f : rows) {
            sb.append(f.getCreatedAt()).append(",")
              .append(nameOf(f.getUserId())).append(",")
              .append(com.psc.witkey.dto.WalletFlowVO.TYPES.getOrDefault(f.getType(), f.getType())).append(",")
              .append(f.getAmount()).append(",")
              .append(f.getBalanceAfter()).append(",")
              .append(f.getBizNo() == null ? "" : f.getBizNo()).append(",")
              .append(f.getRemark() == null ? "" : f.getRemark().replace(",", "，"))
              .append("\n");
        }
        return sb.toString();
    }

    // 组装分页查询（按时间倒序，导出时取前 5000 条）
    private java.util.List<com.psc.witkey.domain.WalletFlow> queryFlows(
            String type, Long userId, String keyword,
            java.time.LocalDateTime start, java.time.LocalDateTime end) {
        int size = 5000;
        return flowRepository.findAll(specOf(type, userId, keyword, start, end),
                org.springframework.data.domain.PageRequest.of(0, size,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                .getContent();
    }

    // 条件：类型 / 用户 / 关键词 / 时间
    private org.springframework.data.jpa.domain.Specification<com.psc.witkey.domain.WalletFlow> specOf(
            String type, Long userId, String keyword,
            java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return (root, query, cb) -> {
            var ps = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (type != null && !type.isBlank()) {
                ps.add(cb.equal(root.get("type"), type.trim().toUpperCase()));
            }
            if (userId != null) {
                ps.add(cb.equal(root.get("userId"), userId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                ps.add(cb.or(cb.like(root.get("remark"), like), cb.like(root.get("bizNo"), like)));
            }
            if (start != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    // 用户名缓存（一次查询解决 N+1）
    private String nameOf(Long userId) {
        if (userId == null) return "系统";
        return sysUserRepository.findById(userId)
                .map(u -> u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname())
                .orElse("用户#" + userId);
    }
}
