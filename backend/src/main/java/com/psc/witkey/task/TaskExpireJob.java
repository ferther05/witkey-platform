package com.psc.witkey.task;

import com.psc.witkey.domain.Task;
import com.psc.witkey.domain.TaskStatus;
import com.psc.witkey.repository.TaskRepository;
import com.psc.witkey.service.NoticeService;
import com.psc.witkey.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：招募中但已过截止时间的任务 → 自动流标，赏金解冻退回雇主。
 * 每 10 分钟扫一次，数量少、逻辑简单，不需要分布式调度。
 */
@Component
public class TaskExpireJob {

    private static final Logger log = LoggerFactory.getLogger(TaskExpireJob.class);

    private final TaskRepository taskRepository;
    private final WalletService walletService;
    private final NoticeService noticeService;

    public TaskExpireJob(TaskRepository taskRepository, WalletService walletService,
                         NoticeService noticeService) {
        this.taskRepository = taskRepository;
        this.walletService = walletService;
        this.noticeService = noticeService;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void flowExpiredTasks() {
        List<Task> expired = taskRepository.findByStatusAndDeadlineBefore(
                TaskStatus.RECRUITING.name(), LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        for (Task task : expired) {
            task.setStatus(TaskStatus.FLOWED.name());
            taskRepository.save(task);
            walletService.unfreeze(task.getEmployerId(), task.getBudget(),
                    "TASK-" + task.getId() + "-FLOWED-" + LocalDateTime.now().toLocalDate(),
                    "任务《" + task.getTitle() + "》到期流标，赏金解冻");
            noticeService.push(task.getEmployerId(), "TRADE", "任务已流标",
                    "任务《" + task.getTitle() + "》已过截止时间，托管赏金 ¥"
                            + task.getBudget().stripTrailingZeros().toPlainString() + " 已退回余额。",
                    "TASK", task.getId());
        }
        log.info("自动流标 {} 个任务", expired.size());
    }
}
