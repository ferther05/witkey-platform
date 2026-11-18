// StatsService —— 业务：数据看板统计与公开概览
package com.psc.witkey.service;

import com.psc.witkey.domain.BizOrder;
import com.psc.witkey.domain.OrderStatus;
import com.psc.witkey.domain.Task;
import com.psc.witkey.domain.TaskCategory;
import com.psc.witkey.dto.DashboardVO;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.ReportRepository;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.repository.TaskCategoryRepository;
import com.psc.witkey.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 看板取数。演示数据量小，直接内存聚合；数据量大了再换 SQL group by。
 */
@Service
public class StatsService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM-dd");

    private final SysUserRepository userRepository;
    private final TaskRepository taskRepository;
    private final BizOrderRepository orderRepository;
    private final ReportRepository reportRepository;
    private final TaskCategoryRepository categoryRepository;
    private final FinanceService financeService;

    public StatsService(SysUserRepository userRepository, TaskRepository taskRepository,
                        BizOrderRepository orderRepository, ReportRepository reportRepository,
                        TaskCategoryRepository categoryRepository, FinanceService financeService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.orderRepository = orderRepository;
        this.reportRepository = reportRepository;
        this.categoryRepository = categoryRepository;
        this.financeService = financeService;
    }

    /** 首页展示用的公开概览 */
    public com.psc.witkey.dto.PublicOverviewVO overview() {
        var finance = financeService.summary();
        return new com.psc.witkey.dto.PublicOverviewVO(
                taskRepository.count(),
                taskRepository.countByStatus("RECRUITING"),
                userRepository.count(),
                orderRepository.count(),
                finance.doneOrderCount(),
                finance.totalGmv(),
                finance.totalCommission());
    }

    // 统计后台看板数据
    public DashboardVO dashboard() {
        var finance = financeService.summary();
        List<BizOrder> orders = orderRepository.findAll();
        List<Task> tasks = taskRepository.findAll();
        List<TaskCategory> categories = categoryRepository.findAllByOrderBySortAsc();

        return new DashboardVO(
                userRepository.count(),
                taskRepository.count(),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.DONE.name()),
                taskRepository.countByStatus("PENDING_AUDIT"),
                reportRepository.countByStatus("PENDING"),
                finance.totalGmv(),
                finance.totalCommission(),
                finance.escrowAmount(),
                buildTrend(orders),
                buildCategoryStats(tasks, categories));
    }

    // 统计近 7 天订单数与成交额
    private List<DashboardVO.TrendPoint> buildTrend(List<BizOrder> orders) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, List<BizOrder>> byDay = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            byDay.put(today.minusDays(i), new ArrayList<>());
        }
        for (BizOrder order : orders) {
            LocalDate day = order.getCreatedAt().toLocalDate();
            byDay.computeIfPresent(day, (k, v) -> {
                v.add(order);
                return v;
            });
        }
        return byDay.entrySet().stream()
                .map(e -> new DashboardVO.TrendPoint(
                        e.getKey().format(DAY),
                        e.getValue().size(),
                        e.getValue().stream().map(BizOrder::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();
    }

    // 统计各分类的任务数量
    private List<DashboardVO.CategoryStat> buildCategoryStats(List<Task> tasks, List<TaskCategory> categories) {
        Map<Long, Long> countByCategory = new java.util.HashMap<>();
        for (Task task : tasks) {
            countByCategory.merge(task.getCategoryId(), 1L, Long::sum);
        }
        return categories.stream()
                .map(c -> new DashboardVO.CategoryStat(c.getName(),
                        countByCategory.getOrDefault(c.getId(), 0L)))
                .toList();
    }
}
