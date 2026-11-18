// OrderController —— 接口：我的订单、订单详情、交付、验收、驳回、待办数量
package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.DeliverRequest;
import com.psc.witkey.dto.OrderVO;
import com.psc.witkey.dto.RejectRequest;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接口：我的订单、订单详情、交付、验收、驳回、待办数量。
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 我的订单：role=employer 我买到的 / role=witkey 我卖出的 */
    @GetMapping("/my")
    // 我的订单
    public Result<List<OrderVO>> myOrders(@RequestParam(defaultValue = "employer") String role) {
        return Result.ok(orderService.myOrders(SecurityUtils.userId(), role));
    }

    /** 待办数量：雇主待验收、威客待交付（首页用） */
    @GetMapping("/todo")
    // 待办数量
    public Result<java.util.Map<String, Long>> todo() {
        Long userId = SecurityUtils.userId();
        return Result.ok(java.util.Map.of(
                "toAccept", orderService.countToAccept(userId),
                "toDeliver", orderService.countToDeliver(userId)));
    }

    // 查询详情
    @GetMapping("/{id}")
    // 查询详情
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(SecurityUtils.userId(), id));
    }

    /** 威客交付 */
    @PostMapping("/{id}/deliver")
    // 威客交付
    public Result<Void> deliver(@PathVariable Long id, @RequestBody @Valid DeliverRequest req) {
        orderService.deliver(SecurityUtils.userId(), id, req.note(), req.fileIds());
        // 操作留痕：交付
        com.psc.witkey.service.OpLog.now("ORDER_DELIVER", "ORDER", id,
                "交付订单 #" + id + "，附件 " + (req.fileIds() == null ? 0 : req.fileIds().size()) + " 个");
        return Result.ok();
    }

    /** 雇主验收通过（触发结算） */
    @PostMapping("/{id}/accept")
    // 验收并结算
    public Result<Void> accept(@PathVariable Long id) {
        orderService.accept(SecurityUtils.userId(), id);
        // 操作留痕：验收通过
        com.psc.witkey.service.OpLog.now("ORDER_ACCEPT", "ORDER", id, "验收通过订单 #" + id + "，已结算");
        return Result.ok();
    }

    /** 雇主驳回，要求修改后重新交付 */
    @PostMapping("/{id}/reject")
    // 驳回验收
    public Result<Void> reject(@PathVariable Long id, @RequestBody @Valid RejectRequest req) {
        orderService.reject(SecurityUtils.userId(), id, req.reason());
        // 操作留痕：驳回验收
        com.psc.witkey.service.OpLog.now("ORDER_REJECT", "ORDER", id, "驳回订单 #" + id + "：" + req.reason());
        return Result.ok();
    }
}
