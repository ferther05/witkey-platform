package com.psc.witkey.controller;

import com.psc.witkey.common.Result;
import com.psc.witkey.dto.BidRequest;
import com.psc.witkey.dto.BidVO;
import com.psc.witkey.security.LoginUser;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.BidService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    /** 威客投标 */
    @PostMapping("/bid")
    public Result<BidVO> submit(@RequestBody @Valid BidRequest req) {
        return Result.ok(bidService.submit(SecurityUtils.userId(), req));
    }

    /** 修改自己的投标（仅待选状态） */
    @PutMapping("/bid/{id}")
    public Result<BidVO> update(@PathVariable Long id, @RequestBody @Valid BidRequest req) {
        return Result.ok(bidService.update(SecurityUtils.userId(), id, req));
    }

    /** 撤回投标 */
    @PostMapping("/bid/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        bidService.withdraw(SecurityUtils.userId(), id);
        return Result.ok();
    }

    /** 我的投标 */
    @GetMapping("/bid/my")
    public Result<List<BidVO>> myBids() {
        return Result.ok(bidService.myBids(SecurityUtils.userId()));
    }

    /** 雇主选标（生成担保订单） */
    @PostMapping("/bid/{id}/select")
    public Result<Map<String, Long>> select(@PathVariable Long id) {
        var order = bidService.select(SecurityUtils.userId(), id);
        return Result.ok(Map.of("orderId", order.getId()));
    }

    /** 雇主查看某任务的投标列表 */
    @GetMapping("/task/{taskId}/bids")
    public Result<List<BidVO>> listByTask(@PathVariable Long taskId) {
        LoginUser viewer = SecurityUtils.current();
        return Result.ok(bidService.listByTask(taskId, viewer.id(), "ADMIN".equals(viewer.role())));
    }
}
