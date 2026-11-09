package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.domain.Bid;
import com.psc.witkey.domain.BizOrder;
import com.psc.witkey.domain.OrderStatus;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.domain.Task;
import com.psc.witkey.domain.TaskCategory;
import com.psc.witkey.domain.TaskStatus;
import com.psc.witkey.dto.OrderVO;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.repository.TaskCategoryRepository;
import com.psc.witkey.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 担保订单：选标生成、交付、验收、驳回、结算。
 * 结算是"托管 -> 扣佣金 -> 威客到账"一条事务，中途任何一步失败全部回滚。
 */
@Service
public class OrderService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BizOrderRepository orderRepository;
    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;
    private final SysUserRepository userRepository;
    private final WalletService walletService;
    private final NoticeService noticeService;
    private final FileService fileService;

    public OrderService(BizOrderRepository orderRepository, TaskRepository taskRepository,
                        TaskCategoryRepository categoryRepository, SysUserRepository userRepository,
                        WalletService walletService, NoticeService noticeService, FileService fileService) {
        this.orderRepository = orderRepository;
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.noticeService = noticeService;
        this.fileService = fileService;
    }

    /** 选标时调用：生成订单并把当时的佣金比例快照下来 */
    @Transactional
    public BizOrder createForWinningBid(Task task, Bid bid) {
        BigDecimal rate = categoryRepository.findById(task.getCategoryId())
                .map(TaskCategory::getCommissionRate)
                .orElse(new BigDecimal("10.00"));
        BigDecimal amount = task.getBudget();
        BigDecimal commission = amount.multiply(rate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BizOrder order = new BizOrder();
        order.setOrderNo(buildOrderNo());
        order.setTaskId(task.getId());
        order.setEmployerId(task.getEmployerId());
        order.setWitkeyId(bid.getWitkeyId());
        order.setAmount(amount);
        order.setCommissionRate(rate);
        order.setCommissionAmount(commission);
        order.setStatus(OrderStatus.TO_DELIVER.name());
        return orderRepository.save(order);
    }

    @Transactional
    public void deliver(Long userId, Long orderId, String note, java.util.List<Long> fileIds) {
        BizOrder order = requireOrder(orderId);
        if (!order.getWitkeyId().equals(userId)) {
            throw new BusinessException(403, "只有中标威客可以交付");
        }
        if (!OrderStatus.TO_DELIVER.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能交付");
        }
        order.setDeliverNote(note.trim());
        if (fileIds != null && !fileIds.isEmpty()) {
            order.setDeliverFileIds(fileIds.stream().map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(",")));
        }
        order.setDeliverCount((order.getDeliverCount() == null ? 0 : order.getDeliverCount()) + 1);
        order.setDeliverTime(LocalDateTime.now());
        order.setStatus(OrderStatus.TO_ACCEPT.name());
        orderRepository.save(order);

        updateTaskStatus(order.getTaskId(), TaskStatus.TO_ACCEPT);
        noticeService.push(order.getEmployerId(), "TRADE", "威客已交付",
                "订单 " + order.getOrderNo() + " 已提交交付物，请尽快验收。", "ORDER", order.getId());
    }

    /**
     * 验收通过：结算给威客（扣佣金），订单完成，任务完成。
     */
    @Transactional
    public void accept(Long userId, Long orderId) {
        BizOrder order = requireOrder(orderId);
        if (!order.getEmployerId().equals(userId)) {
            throw new BusinessException(403, "只有雇主可以验收");
        }
        if (!OrderStatus.TO_ACCEPT.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能验收");
        }
        walletService.settle(order.getEmployerId(), order.getWitkeyId(),
                order.getAmount(), order.getCommissionAmount(),
                "ORDER-" + order.getOrderNo(), "订单 " + order.getOrderNo() + " 结算");

        order.setStatus(OrderStatus.DONE.name());
        order.setAcceptTime(LocalDateTime.now());
        order.setSettleTime(LocalDateTime.now());
        orderRepository.save(order);

        updateTaskStatus(order.getTaskId(), TaskStatus.DONE);

        BigDecimal net = order.getAmount().subtract(order.getCommissionAmount());
        noticeService.push(order.getWitkeyId(), "TRADE", "订单已结算",
                "订单 " + order.getOrderNo() + " 验收通过，¥" + net.stripTrailingZeros().toPlainString()
                        + " 已到账你的钱包（已扣佣金 ¥"
                        + order.getCommissionAmount().stripTrailingZeros().toPlainString() + "）。",
                "ORDER", order.getId());
        noticeService.push(order.getEmployerId(), "TRADE", "订单已完成",
                "订单 " + order.getOrderNo() + " 已完成结算，感谢使用。", "ORDER", order.getId());
    }

    @Transactional
    public void reject(Long userId, Long orderId, String reason) {
        BizOrder order = requireOrder(orderId);
        if (!order.getEmployerId().equals(userId)) {
            throw new BusinessException(403, "只有雇主可以驳回验收");
        }
        if (!OrderStatus.TO_ACCEPT.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能驳回");
        }
        order.setStatus(OrderStatus.TO_DELIVER.name());
        order.setRejectReason(reason.trim());
        orderRepository.save(order);

        updateTaskStatus(order.getTaskId(), TaskStatus.IN_PROGRESS);
        noticeService.push(order.getWitkeyId(), "TRADE", "验收未通过",
                "订单 " + order.getOrderNo() + " 被驳回：" + reason.trim() + "，请修改后重新交付。",
                "ORDER", order.getId());
    }

    public List<OrderVO> myOrders(Long userId, String role) {
        List<BizOrder> orders = "witkey".equalsIgnoreCase(role)
                ? orderRepository.findByWitkeyIdOrderByCreatedAtDesc(userId)
                : orderRepository.findByEmployerIdOrderByCreatedAtDesc(userId);
        return toVOList(orders);
    }

    /** 待我验收的订单数（雇主视角） */
    public long countToAccept(Long userId) {
        return orderRepository.countByEmployerIdAndStatus(userId, OrderStatus.TO_ACCEPT.name());
    }

    /** 待我交付的订单数（威客视角） */
    public long countToDeliver(Long userId) {
        return orderRepository.countByWitkeyIdAndStatus(userId, OrderStatus.TO_DELIVER.name());
    }

    public OrderVO detail(Long userId, Long orderId) {
        BizOrder order = requireOrder(orderId);
        if (!order.getEmployerId().equals(userId) && !order.getWitkeyId().equals(userId)) {
            throw new BusinessException(403, "无权查看该订单");
        }
        return toVOList(List.of(order)).get(0);
    }

    // ------------------------------------------------------------------ 内部

    private BizOrder requireOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }

    private void updateTaskStatus(Long taskId, TaskStatus status) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(status.name());
            taskRepository.save(task);
        });
    }

    private String buildOrderNo() {
        return "WK" + LocalDateTime.now().format(NO_FMT)
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private List<OrderVO> toVOList(List<BizOrder> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<Long, Task> tasks = taskRepository.findAllById(
                        orders.stream().map(BizOrder::getTaskId).distinct().toList()).stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
        Map<Long, String> users = userRepository.findAllById(
                        orders.stream().flatMap(o -> java.util.stream.Stream.of(o.getEmployerId(), o.getWitkeyId()))
                                .distinct().toList()).stream()
                .collect(Collectors.toMap(SysUser::getId, this::displayName));

        return orders.stream().map(order -> {
            Task task = tasks.get(order.getTaskId());
            java.util.List<Long> fileIds = order.getDeliverFileIds() == null || order.getDeliverFileIds().isBlank()
                    ? java.util.List.of()
                    : java.util.Arrays.stream(order.getDeliverFileIds().split(","))
                    .filter(s -> !s.isBlank()).map(Long::valueOf).toList();
            return new OrderVO(
                    order.getId(), order.getOrderNo(), order.getTaskId(),
                    task == null ? "任务已删除" : task.getTitle(),
                    task == null ? "" : task.getDescription(),
                    order.getEmployerId(), users.getOrDefault(order.getEmployerId(), "-"),
                    order.getWitkeyId(), users.getOrDefault(order.getWitkeyId(), "-"),
                    order.getAmount(), order.getCommissionRate(), order.getCommissionAmount(),
                    order.getStatus(), order.getDeliverNote(), order.getDeliverCount(),
                    order.getRejectReason(), fileService.listByIds(fileIds),
                    order.getDeliverTime(), order.getAcceptTime(),
                    order.getSettleTime(), order.getCreatedAt());
        }).toList();
    }

    private String displayName(SysUser user) {
        return user.getNickname() == null || user.getNickname().isBlank()
                ? user.getUsername() : user.getNickname();
    }
}
