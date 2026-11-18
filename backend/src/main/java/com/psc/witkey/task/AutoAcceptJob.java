// AutoAcceptJob —— 定时任务：交付超时未验收则自动结算
package com.psc.witkey.task;

import com.psc.witkey.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import com.psc.witkey.service.OpLog;
import org.springframework.stereotype.Component;

/**
 * 超时自动验收（工作项 3.7）。
 *
 * 威客交付后，雇主超过 {@code witkey.order.auto-accept-days} 天既不验收也不驳回，
 * 系统自动按验收通过处理并结算，避免赏金长期滞留托管。
 */
@Component
public class AutoAcceptJob {

    private static final Logger log = LoggerFactory.getLogger(AutoAcceptJob.class);

    private final OrderService orderService;

    @Value("${witkey.order.auto-accept-days:7}")
    private int autoAcceptDays;

    public AutoAcceptJob(OrderService orderService) {
        this.orderService = orderService;
    }

    // 定时任务入口
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    // 定时任务入口
    public void run() {
        int handled = orderService.autoAcceptExpired(autoAcceptDays);
        // 操作留痕：系统自动验收
        if (handled > 0) {
            OpLog.of(null, "ORDER_AUTO_ACCEPT", "ORDER", null, "系统自动验收 " + handled + " 单（阈值 " + autoAcceptDays + " 天）");
            log.info("超时自动验收：处理 {} 个订单（阈值 {} 天）", handled, autoAcceptDays);
        }
    }
}
