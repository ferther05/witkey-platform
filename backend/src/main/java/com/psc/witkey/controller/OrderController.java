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

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 我的订单：role=employer 我买到的 / role=witkey 我卖出的 */
    @GetMapping("/my")
    public Result<List<OrderVO>> myOrders(@RequestParam(defaultValue = "employer") String role) {
        return Result.ok(orderService.myOrders(SecurityUtils.userId(), role));
    }

    /** 待办数量：雇主待验收、威客待交付（首页用） */
    @GetMapping("/todo")
    public Result<java.util.Map<String, Long>> todo() {
        Long userId = SecurityUtils.userId();
        return Result.ok(java.util.Map.of(
                "toAccept", orderService.countToAccept(userId),
                "toDeliver", orderService.countToDeliver(userId)));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(SecurityUtils.userId(), id));
    }

    /** 威客交付 */
    @PostMapping("/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id, @RequestBody @Valid DeliverRequest req) {
        orderService.deliver(SecurityUtils.userId(), id, req.note(), req.fileIds());
        return Result.ok();
    }

    /** 雇主验收通过（触发结算） */
    @PostMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        orderService.accept(SecurityUtils.userId(), id);
        return Result.ok();
    }

    /** 雇主驳回，要求修改后重新交付 */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody @Valid RejectRequest req) {
        orderService.reject(SecurityUtils.userId(), id, req.reason());
        return Result.ok();
    }
}
