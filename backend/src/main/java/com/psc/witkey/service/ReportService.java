package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.BizOrder;
import com.psc.witkey.domain.OrderStatus;
import com.psc.witkey.domain.Report;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.domain.Task;
import com.psc.witkey.domain.TaskStatus;
import com.psc.witkey.dto.CreateReportRequest;
import com.psc.witkey.dto.ReportVO;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.ReportRepository;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 举报：前台提交，后台处理。
 * 处理动作带有副作用：下架任务、封禁用户、订单强制退款/打款——这是本项目里纠纷的唯一出口。
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final SysUserRepository userRepository;
    private final TaskRepository taskRepository;
    private final BizOrderRepository orderRepository;
    private final WalletService walletService;
    private final NoticeService noticeService;
    private final FileService fileService;

    public ReportService(ReportRepository reportRepository, SysUserRepository userRepository,
                         TaskRepository taskRepository, BizOrderRepository orderRepository,
                         WalletService walletService, NoticeService noticeService, FileService fileService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.noticeService = noticeService;
        this.fileService = fileService;
    }

    @Transactional
    public void submit(Long userId, CreateReportRequest req) {
        // 校验举报对象确实存在，避免垃圾数据
        switch (req.targetType()) {
            case "TASK" -> taskRepository.findById(req.targetId())
                    .orElseThrow(() -> new BusinessException(404, "举报的任务不存在"));
            case "ORDER" -> orderRepository.findById(req.targetId())
                    .orElseThrow(() -> new BusinessException(404, "举报的订单不存在"));
            case "USER" -> userRepository.findById(req.targetId())
                    .orElseThrow(() -> new BusinessException(404, "举报的用户不存在"));
            default -> throw new BusinessException("举报对象类型不合法");
        }

        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(req.targetType());
        report.setTargetId(req.targetId());
        report.setReason(req.reason().trim());
        if (req.fileIds() != null && !req.fileIds().isEmpty()) {
            report.setFileIds(req.fileIds().stream().map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
        report.setStatus("PENDING");
        reportRepository.save(report);
    }

    public PageResult<ReportVO> page(String status, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        Page<Report> result = (status == null || status.isBlank())
                ? reportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(p - 1, s))
                : reportRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(p - 1, s));
        return new PageResult<>(toVOList(result.getContent()), result.getTotalElements(), p, s);
    }

    @Transactional
    public void handle(Long reportId, String action, String remark) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "举报不存在"));
        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException("该举报已处理");
        }

        switch (action) {
            case "REJECT" -> report.setHandleOrderAction("NONE");
            case "TAKE_DOWN" -> takeDown(report);
            case "BAN" -> ban(report);
            case "REFUND" -> refund(report);
            case "PAY" -> pay(report);
            default -> throw new BusinessException("处理方式不合法");
        }

        report.setStatus("DONE");
        report.setHandleRemark(remark == null ? null : remark.trim());
        report.setHandledAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    // ------------------------------------------------------------------ 处理动作

    private void takeDown(Report report) {
        if (!"TASK".equals(report.getTargetType())) {
            throw new BusinessException("只有举报任务才能下架");
        }
        Task task = taskRepository.findById(report.getTargetId())
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));
        if (!(TaskStatus.RECRUITING.name().equals(task.getStatus())
                || TaskStatus.PENDING_AUDIT.name().equals(task.getStatus()))) {
            throw new BusinessException("任务已进入交易流程，请改用订单退款/打款处理");
        }
        task.setStatus(TaskStatus.WITHDRAWN.name());
        task.setAuditRemark("因举报被平台下架：" + report.getReason());
        taskRepository.save(task);
        walletService.unfreeze(task.getEmployerId(), task.getBudget(),
                "TASK-" + task.getId() + "-REPORT-" + report.getId(), "举报下架，赏金解冻");
        noticeService.push(task.getEmployerId(), "SYSTEM", "任务已被下架",
                "你的任务《" + task.getTitle() + "》因举报被平台下架，托管赏金已退回余额。", "TASK", task.getId());
    }

    private void ban(Report report) {
        if (!"USER".equals(report.getTargetType())) {
            throw new BusinessException("只有举报用户才能封禁");
        }
        SysUser user = userRepository.findById(report.getTargetId())
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setStatus(0);
        userRepository.save(user);
    }

    private void refund(Report report) {
        BizOrder order = requireOrderForAction(report);
        if (!(OrderStatus.TO_DELIVER.name().equals(order.getStatus())
                || OrderStatus.TO_ACCEPT.name().equals(order.getStatus()))) {
            throw new BusinessException("该订单当前状态不支持退款");
        }
        walletService.unfreeze(order.getEmployerId(), order.getAmount(),
                "ORDER-" + order.getOrderNo() + "-REFUND", "举报处理：订单强制退款");
        order.setStatus(OrderStatus.REFUNDED.name());
        orderRepository.save(order);

        taskRepository.findById(order.getTaskId()).ifPresent(task -> {
            task.setStatus(TaskStatus.WITHDRAWN.name());
            taskRepository.save(task);
        });
        noticeService.push(order.getEmployerId(), "TRADE", "订单已退款",
                "订单 " + order.getOrderNo() + " 经平台处理已退款，托管赏金已退回余额。", "ORDER", order.getId());
        noticeService.push(order.getWitkeyId(), "TRADE", "订单被退款",
                "订单 " + order.getOrderNo() + " 经平台处理已退款。", "ORDER", order.getId());
        report.setHandleOrderAction("REFUND");
    }

    private void pay(Report report) {
        BizOrder order = requireOrderForAction(report);
        if (!(OrderStatus.TO_DELIVER.name().equals(order.getStatus())
                || OrderStatus.TO_ACCEPT.name().equals(order.getStatus()))) {
            throw new BusinessException("该订单当前状态不支持强制打款");
        }
        walletService.settle(order.getEmployerId(), order.getWitkeyId(),
                order.getAmount(), order.getCommissionAmount(),
                "ORDER-" + order.getOrderNo() + "-FORCE", "举报处理：订单强制打款");
        order.setStatus(OrderStatus.DONE.name());
        order.setAcceptTime(LocalDateTime.now());
        order.setSettleTime(LocalDateTime.now());
        orderRepository.save(order);

        taskRepository.findById(order.getTaskId()).ifPresent(task -> {
            task.setStatus(TaskStatus.DONE.name());
            taskRepository.save(task);
        });
        noticeService.push(order.getWitkeyId(), "TRADE", "平台已强制结算",
                "订单 " + order.getOrderNo() + " 经平台处理已结算，款项已到账。", "ORDER", order.getId());
        report.setHandleOrderAction("PAY");
    }

    private BizOrder requireOrderForAction(Report report) {
        if (!"ORDER".equals(report.getTargetType())) {
            throw new BusinessException("只有举报订单才能执行退款/打款");
        }
        return orderRepository.findById(report.getTargetId())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }

    // ------------------------------------------------------------------ 转换

    private List<ReportVO> toVOList(List<Report> reports) {
        if (reports.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> users = userRepository.findAllById(
                        reports.stream().map(Report::getReporterId).distinct().toList()).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, Task> tasks = taskRepository.findAllById(
                        reports.stream().filter(r -> "TASK".equals(r.getTargetType()))
                                .map(Report::getTargetId).distinct().toList()).stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
        Map<Long, BizOrder> orders = orderRepository.findAllById(
                        reports.stream().filter(r -> "ORDER".equals(r.getTargetType()))
                                .map(Report::getTargetId).distinct().toList()).stream()
                .collect(Collectors.toMap(BizOrder::getId, Function.identity()));

        return reports.stream().map(r -> {
            SysUser reporter = users.get(r.getReporterId());
            String reporterName = reporter == null ? "-"
                    : (reporter.getNickname() == null || reporter.getNickname().isBlank()
                    ? reporter.getUsername() : reporter.getNickname());
            String targetTitle = switch (r.getTargetType()) {
                case "TASK" -> tasks.containsKey(r.getTargetId())
                        ? tasks.get(r.getTargetId()).getTitle() : "任务 #" + r.getTargetId();
                case "ORDER" -> orders.containsKey(r.getTargetId())
                        ? orders.get(r.getTargetId()).getOrderNo() : "订单 #" + r.getTargetId();
                default -> "用户 #" + r.getTargetId();
            };
            List<Long> fileIds = r.getFileIds() == null || r.getFileIds().isBlank() ? List.of()
                    : Arrays.stream(r.getFileIds().split(",")).filter(x -> !x.isBlank()).map(Long::valueOf).toList();
            return new ReportVO(r.getId(), r.getReporterId(), reporterName,
                    r.getTargetType(), r.getTargetId(), targetTitle, r.getReason(),
                    fileService.listByIds(fileIds), r.getStatus(), r.getHandleRemark(),
                    r.getHandleOrderAction(), r.getCreatedAt(), r.getHandledAt());
        }).toList();
    }
}
