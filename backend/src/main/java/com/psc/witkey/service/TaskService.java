package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.SysUser;
import com.psc.witkey.domain.Task;
import com.psc.witkey.domain.TaskCategory;
import com.psc.witkey.domain.TaskFavorite;
import com.psc.witkey.domain.TaskStatus;
import com.psc.witkey.dto.TaskPublishRequest;
import com.psc.witkey.dto.TaskVO;
import com.psc.witkey.repository.SysUserRepository;
import com.psc.witkey.repository.TaskCategoryRepository;
import com.psc.witkey.repository.TaskFavoriteRepository;
import com.psc.witkey.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务核心服务：发布（冻结赏金）、编辑（重新审核）、撤回（解冻）、大厅查询、审核。
 * 状态只在这里改，保证"谁能改、从哪个状态改到哪个状态"只有一处实现。
 */
@Service
public class TaskService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;
    private final TaskFavoriteRepository favoriteRepository;
    private final SysUserRepository userRepository;
    private final WalletService walletService;
    private final NoticeService noticeService;

    public TaskService(TaskRepository taskRepository,
                       TaskCategoryRepository categoryRepository,
                       TaskFavoriteRepository favoriteRepository,
                       SysUserRepository userRepository,
                       WalletService walletService,
                       NoticeService noticeService) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.noticeService = noticeService;
    }

    // ------------------------------------------------------------------ 写

    @Transactional
    public TaskVO publish(Long userId, TaskPublishRequest req) {
        TaskCategory category = requireCategory(req.categoryId());

        Task task = new Task();
        task.setTitle(req.title().trim());
        task.setCategoryId(category.getId());
        task.setEmployerId(userId);
        task.setDescription(req.description().trim());
        task.setBudget(req.budget());
        task.setDeadline(req.deadline());
        task.setStatus(TaskStatus.PENDING_AUDIT.name());
        Task saved = taskRepository.save(task);

        // 发布即冻结赏金：钱从余额进 frozen，撤回/流标时原路退回
        walletService.freeze(userId, req.budget(), "TASK-" + saved.getId() + "-FREEZE",
                "发布任务《" + saved.getTitle() + "》冻结赏金");

        return toVO(saved, false);
    }

    @Transactional
    public TaskVO update(Long userId, Long taskId, TaskPublishRequest req) {
        Task task = requireOwnedTask(userId, taskId);
        if (!TaskStatus.valueOf(task.getStatus()).canEdit()) {
            throw new BusinessException("当前状态（" + task.getStatus() + "）不允许编辑");
        }
        if (task.getBidCount() != null && task.getBidCount() > 0) {
            throw new BusinessException("已有威客投标，不能再修改任务");
        }
        TaskCategory category = requireCategory(req.categoryId());

        // 赏金变化需要同步调整冻结金额，否则托管金额与任务对不上
        BigDecimal oldBudget = task.getBudget();
        BigDecimal newBudget = req.budget();
        int cmp = newBudget.compareTo(oldBudget);
        if (cmp != 0) {
            String stamp = LocalDateTime.now().format(NO_FMT);
            if (cmp > 0) {
                walletService.freeze(userId, newBudget.subtract(oldBudget),
                        "TASK-" + taskId + "-EDIT-" + stamp, "编辑任务追加冻结");
            } else {
                walletService.unfreeze(userId, oldBudget.subtract(newBudget),
                        "TASK-" + taskId + "-EDIT-" + stamp, "编辑任务退还差额");
            }
        }

        task.setTitle(req.title().trim());
        task.setCategoryId(category.getId());
        task.setDescription(req.description().trim());
        task.setBudget(newBudget);
        task.setDeadline(req.deadline());
        // 改过的任务必须重新审核，避免先通过再改内容绕过审核
        task.setStatus(TaskStatus.PENDING_AUDIT.name());
        task.setAuditRemark(null);
        return toVO(taskRepository.save(task), false);
    }

    @Transactional
    public void withdraw(Long userId, Long taskId) {
        Task task = requireOwnedTask(userId, taskId);
        TaskStatus status = TaskStatus.valueOf(task.getStatus());
        if (status == TaskStatus.WITHDRAWN) {
            throw new BusinessException("任务已撤回");
        }
        if (!(status == TaskStatus.PENDING_AUDIT || status == TaskStatus.REJECTED
                || status == TaskStatus.RECRUITING)) {
            throw new BusinessException("任务已进入交易流程，不能撤回");
        }
        if (task.getBidCount() != null && task.getBidCount() > 0) {
            throw new BusinessException("已有威客投标，不能直接撤回，请联系客服处理");
        }
        task.setStatus(TaskStatus.WITHDRAWN.name());
        taskRepository.save(task);
        walletService.unfreeze(userId, task.getBudget(),
                "TASK-" + taskId + "-UNFREEZE-" + LocalDateTime.now().format(NO_FMT),
                "撤回任务《" + task.getTitle() + "》退还赏金");
    }

    @Transactional
    public TaskVO audit(Long taskId, boolean pass, String remark) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));
        if (!TaskStatus.PENDING_AUDIT.name().equals(task.getStatus())) {
            throw new BusinessException("只有待审核的任务可以审核");
        }
        if (pass) {
            task.setStatus(TaskStatus.RECRUITING.name());
            task.setAuditRemark(remark);
            noticeService.push(task.getEmployerId(), "TRADE", "任务审核通过",
                    "你的任务《" + task.getTitle() + "》已通过审核，现已在大厅展示。", "TASK", task.getId());
        } else {
            if (remark == null || remark.isBlank()) {
                throw new BusinessException("驳回需要填写理由");
            }
            task.setStatus(TaskStatus.REJECTED.name());
            task.setAuditRemark(remark.trim());
            noticeService.push(task.getEmployerId(), "TRADE", "任务审核未通过",
                    "你的任务《" + task.getTitle() + "》未通过审核：" + remark.trim() + "，修改后可重新提交。",
                    "TASK", task.getId());
        }
        return toVO(taskRepository.save(task), false);
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long taskId) {
        taskRepository.findById(taskId).orElseThrow(() -> new BusinessException(404, "任务不存在"));
        var existing = favoriteRepository.findByUserIdAndTaskId(userId, taskId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }
        TaskFavorite favorite = new TaskFavorite();
        favorite.setUserId(userId);
        favorite.setTaskId(taskId);
        favoriteRepository.save(favorite);
        return true;
    }

    // ------------------------------------------------------------------ 读

    public PageResult<TaskVO> hall(String keyword, Long categoryId, BigDecimal minBudget,
                                   BigDecimal maxBudget, String sort, Long viewerId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), TaskStatus.RECRUITING.name()));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(cb.like(root.get("title"), like), cb.like(root.get("description"), like)));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (minBudget != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("budget"), minBudget));
            }
            if (maxBudget != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("budget"), maxBudget));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sortBy = switch (sort == null ? "latest" : sort) {
            case "budget" -> Sort.by(Sort.Direction.DESC, "budget");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadline");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Page<Task> result = taskRepository.findAll(spec, PageRequest.of(p - 1, s, sortBy));
        return new PageResult<>(toVOList(result.getContent(), viewerId), result.getTotalElements(), p, s);
    }

    @Transactional
    public TaskVO detail(Long taskId, Long viewerId, boolean viewerIsAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));

        boolean visible = TaskStatus.RECRUITING.name().equals(task.getStatus())
                || "SELECTED".equals(task.getStatus())
                || "IN_PROGRESS".equals(task.getStatus())
                || "TO_ACCEPT".equals(task.getStatus())
                || "DONE".equals(task.getStatus())
                || (viewerId != null && viewerId.equals(task.getEmployerId()))
                || viewerIsAdmin;
        if (!visible) {
            throw new BusinessException(404, "任务不存在或未上架");
        }

        task.setViewCount((task.getViewCount() == null ? 0 : task.getViewCount()) + 1);
        taskRepository.save(task);

        boolean favorite = viewerId != null && favoriteRepository.existsByUserIdAndTaskId(viewerId, taskId);
        return toVO(task, favorite);
    }

    public PageResult<TaskVO> myTasks(Long userId, String status, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        Page<Task> result = StringUtils.hasText(status)
                ? taskRepository.findByEmployerIdAndStatus(userId, status, PageRequest.of(p - 1, s,
                Sort.by(Sort.Direction.DESC, "createdAt")))
                : taskRepository.findByEmployerId(userId, PageRequest.of(p - 1, s,
                Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(toVOList(result.getContent(), userId), result.getTotalElements(), p, s);
    }

    public PageResult<TaskVO> myFavorites(Long userId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        Page<TaskFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(p - 1, s));
        List<Long> ids = favorites.getContent().stream().map(TaskFavorite::getTaskId).toList();
        List<Task> tasks = ids.isEmpty() ? List.of() : taskRepository.findAllById(ids);
        // 保持收藏时间倒序
        Map<Long, Task> byId = tasks.stream().collect(Collectors.toMap(Task::getId, Function.identity()));
        List<Task> ordered = ids.stream().map(byId::get).filter(java.util.Objects::nonNull).toList();
        return new PageResult<>(toVOList(ordered, userId), favorites.getTotalElements(), p, s);
    }

    public PageResult<TaskVO> pendingAudit(int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        Page<Task> result = taskRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("status"), TaskStatus.PENDING_AUDIT.name()),
                PageRequest.of(p - 1, s, Sort.by(Sort.Direction.ASC, "createdAt")));
        return new PageResult<>(toVOList(result.getContent(), null), result.getTotalElements(), p, s);
    }

    // ------------------------------------------------------------------ 内部

    private TaskCategory requireCategory(Long categoryId) {
        TaskCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("任务分类不存在"));
        if (category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("该分类已停用");
        }
        return category;
    }

    private Task requireOwnedTask(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));
        if (!task.getEmployerId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己发布的任务");
        }
        return task;
    }

    private TaskVO toVO(Task task, boolean favorite) {
        return toVOList(List.of(task), null, favorite).get(0);
    }

    private List<TaskVO> toVOList(List<Task> tasks, Long viewerId) {
        return toVOList(tasks, viewerId, false);
    }

    /**
     * 批量补齐分类名和雇主昵称，避免 N+1 查询。
     */
    private List<TaskVO> toVOList(List<Task> tasks, Long viewerId, boolean forceFavorite) {
        if (tasks.isEmpty()) {
            return List.of();
        }
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (Task task : tasks) {
            categoryIds.add(task.getCategoryId());
            userIds.add(task.getEmployerId());
        }
        Map<Long, String> categoryNames = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(TaskCategory::getId, TaskCategory::getName));
        Map<Long, String> userNames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, this::displayName));

        Set<Long> favoriteIds = new HashSet<>();
        if (viewerId != null && !forceFavorite) {
            List<Long> ids = tasks.stream().map(Task::getId).toList();
            favoriteRepository.findByUserIdAndTaskIdIn(viewerId, ids)
                    .forEach(f -> favoriteIds.add(f.getTaskId()));
        }

        return tasks.stream().map(task -> new TaskVO(
                task.getId(),
                task.getTitle(),
                task.getCategoryId(),
                categoryNames.getOrDefault(task.getCategoryId(), "未知分类"),
                task.getEmployerId(),
                userNames.getOrDefault(task.getEmployerId(), "匿名用户"),
                task.getDescription(),
                task.getBudget(),
                task.getDeadline(),
                task.getStatus(),
                task.getAuditRemark(),
                task.getBidCount(),
                task.getViewCount(),
                forceFavorite || favoriteIds.contains(task.getId()),
                task.getCreatedAt()
        )).toList();
    }

    private String displayName(SysUser user) {
        return user.getNickname() == null || user.getNickname().isBlank()
                ? user.getUsername() : user.getNickname();
    }
}
