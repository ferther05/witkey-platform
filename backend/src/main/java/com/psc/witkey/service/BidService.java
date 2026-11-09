package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.domain.Bid;
import com.psc.witkey.domain.BidStatus;
import com.psc.witkey.domain.BizOrder;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.domain.Task;
import com.psc.witkey.domain.TaskStatus;
import com.psc.witkey.dto.BidRequest;
import com.psc.witkey.dto.BidVO;
import com.psc.witkey.repository.BidRepository;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 投标：威客投标/改标/撤标，雇主查看投标并选标。
 * 选标是并发敏感操作，靠"任务状态条件 + 订单唯一索引"双重保证只成功一次。
 */
@Service
public class BidService {

    private final BidRepository bidRepository;
    private final TaskRepository taskRepository;
    private final SysUserRepository userRepository;
    private final BizOrderRepository orderRepository;
    private final OrderService orderService;
    private final NoticeService noticeService;

    public BidService(BidRepository bidRepository, TaskRepository taskRepository,
                      SysUserRepository userRepository, BizOrderRepository orderRepository,
                      OrderService orderService, NoticeService noticeService) {
        this.bidRepository = bidRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.noticeService = noticeService;
    }

    @Transactional
    public BidVO submit(Long userId, BidRequest req) {
        Task task = taskRepository.findById(req.taskId())
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));
        if (!TaskStatus.RECRUITING.name().equals(task.getStatus())) {
            throw new BusinessException("该任务当前不能投标");
        }
        if (task.getEmployerId().equals(userId)) {
            throw new BusinessException("不能给自己的任务投标");
        }
        if (task.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("任务已截止，无法投标");
        }
        if (bidRepository.findByTaskIdAndWitkeyId(req.taskId(), userId).isPresent()) {
            throw new BusinessException("你已经投过这个任务，可以修改方案");
        }
        if (req.quote().compareTo(task.getBudget()) > 0) {
            throw new BusinessException("报价不能高于任务赏金");
        }

        Bid bid = new Bid();
        bid.setTaskId(task.getId());
        bid.setWitkeyId(userId);
        bid.setQuote(req.quote());
        bid.setContent(req.content().trim());
        bid.setStatus(BidStatus.PENDING.name());
        Bid saved = bidRepository.save(bid);

        refreshBidCount(task);
        noticeService.push(task.getEmployerId(), "TRADE", "收到新的投标",
                "你的任务《" + task.getTitle() + "》收到 1 份新方案，可前往任务详情查看对比。",
                "TASK", task.getId());

        return toVO(saved, task);
    }

    @Transactional
    public BidVO update(Long userId, Long bidId, BidRequest req) {
        Bid bid = requireOwnBid(userId, bidId);
        if (!BidStatus.PENDING.name().equals(bid.getStatus())) {
            throw new BusinessException("只有待选状态的投标可以修改");
        }
        Task task = taskRepository.findById(bid.getTaskId())
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));
        if (!TaskStatus.RECRUITING.name().equals(task.getStatus())) {
            throw new BusinessException("任务已不在招募中，无法修改投标");
        }
        if (req.quote().compareTo(task.getBudget()) > 0) {
            throw new BusinessException("报价不能高于任务赏金");
        }
        bid.setQuote(req.quote());
        bid.setContent(req.content().trim());
        return toVO(bidRepository.save(bid), task);
    }

    @Transactional
    public void withdraw(Long userId, Long bidId) {
        Bid bid = requireOwnBid(userId, bidId);
        if (!BidStatus.PENDING.name().equals(bid.getStatus())) {
            throw new BusinessException("当前状态不能撤回投标");
        }
        bid.setStatus(BidStatus.WITHDRAWN.name());
        bidRepository.save(bid);
        taskRepository.findById(bid.getTaskId()).ifPresent(this::refreshBidCount);
    }

    /**
     * 雇主选标：中标者 WIN，其余 LOSE，任务进入进行中，同时生成担保订单。
     */
    @Transactional
    public BizOrder select(Long userId, Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new BusinessException(404, "投标不存在"));
        Task task = taskRepository.findById(bid.getTaskId())
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));

        if (!task.getEmployerId().equals(userId)) {
            throw new BusinessException(403, "只有雇主可以选标");
        }
        if (!TaskStatus.RECRUITING.name().equals(task.getStatus())) {
            throw new BusinessException("该任务已完成选标或不在招募中");
        }
        if (!BidStatus.PENDING.name().equals(bid.getStatus())) {
            throw new BusinessException("该投标已失效");
        }

        bid.setStatus(BidStatus.WIN.name());
        bidRepository.save(bid);

        for (Bid other : bidRepository.findByTaskIdOrderByCreatedAtAsc(task.getId())) {
            if (!other.getId().equals(bid.getId()) && BidStatus.PENDING.name().equals(other.getStatus())) {
                other.setStatus(BidStatus.LOSE.name());
                bidRepository.save(other);
            }
        }

        task.setStatus(TaskStatus.IN_PROGRESS.name());
        taskRepository.save(task);

        BizOrder order = orderService.createForWinningBid(task, bid);

        noticeService.push(bid.getWitkeyId(), "TRADE", "恭喜你中标",
                "你投标的任务《" + task.getTitle() + "》已被选中，订单 " + order.getOrderNo()
                        + " 已生成，请尽快交付。", "ORDER", order.getId());

        return order;
    }

    public List<BidVO> listByTask(Long taskId, Long viewerId, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));
        if (!isAdmin && !task.getEmployerId().equals(viewerId)) {
            throw new BusinessException(403, "只有雇主可以查看该任务的投标");
        }
        return toVOList(bidRepository.findByTaskIdOrderByCreatedAtAsc(taskId), task);
    }

    public List<BidVO> myBids(Long userId) {
        List<Bid> bids = bidRepository.findByWitkeyIdOrderByCreatedAtDesc(userId);
        if (bids.isEmpty()) {
            return List.of();
        }
        Map<Long, Task> tasks = taskRepository.findAllById(
                        bids.stream().map(Bid::getTaskId).distinct().toList()).stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
        return bids.stream()
                .map(b -> {
                    Task task = tasks.get(b.getTaskId());
                    return task == null ? null : toVO(b, task);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // ------------------------------------------------------------------ 内部

    private Bid requireOwnBid(Long userId, Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new BusinessException(404, "投标不存在"));
        if (!bid.getWitkeyId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己的投标");
        }
        return bid;
    }

    private void refreshBidCount(Task task) {
        long count = bidRepository.countByTaskIdAndStatus(task.getId(), BidStatus.PENDING.name());
        task.setBidCount((int) count);
        taskRepository.save(task);
    }

    private BidVO toVO(Bid bid, Task task) {
        return toVOList(List.of(bid), task).get(0);
    }

    private List<BidVO> toVOList(List<Bid> bids, Task task) {
        if (bids.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> users = userRepository.findAllById(
                        bids.stream().map(Bid::getWitkeyId).distinct().toList()).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        BizOrder order = orderRepository.findByTaskId(task.getId()).orElse(null);

        return bids.stream().map(bid -> {
            SysUser user = users.get(bid.getWitkeyId());
            String name = user == null ? "未知用户"
                    : (user.getNickname() == null || user.getNickname().isBlank()
                    ? user.getUsername() : user.getNickname());
            String skills = user == null ? null : user.getSkills();
            long done = orderRepository.countByWitkeyIdAndStatus(bid.getWitkeyId(),
                    com.psc.witkey.domain.OrderStatus.DONE.name());
            Long orderId = (order != null && BidStatus.WIN.name().equals(bid.getStatus()))
                    ? order.getId() : null;
            return new BidVO(
                    bid.getId(), bid.getTaskId(), task.getTitle(), task.getBudget(),
                    bid.getWitkeyId(), name, skills, done,
                    bid.getQuote(), bid.getContent(), bid.getStatus(), orderId, bid.getCreatedAt());
        }).toList();
    }
}
