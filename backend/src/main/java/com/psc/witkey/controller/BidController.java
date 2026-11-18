// BidController —— 接口：威客投标、改标、撤标、雇主选标、投标列表
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

/**
 * 接口：威客投标、改标、撤标、雇主选标、投标列表。
 */
@RestController
@RequestMapping("/api")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    /** 威客投标 */
    @PostMapping("/bid")
    // 提交
    public Result<BidVO> submit(@RequestBody @Valid BidRequest req) {
        var vo = bidService.submit(SecurityUtils.userId(), req);
        // 操作留痕：提交投标
        com.psc.witkey.service.OpLog.now("BID_CREATE", "BID", vo.id(),
                "投标任务 #" + vo.taskId() + "，报价 " + vo.quote());
        return Result.ok(vo);
    }

    /** 修改自己的投标（仅待选状态） */
    @PutMapping("/bid/{id}")
    // 更新数据
    public Result<BidVO> update(@PathVariable Long id, @RequestBody @Valid BidRequest req) {
        var vo = bidService.update(SecurityUtils.userId(), id, req);
        // 操作留痕：修改投标
        com.psc.witkey.service.OpLog.now("BID_UPDATE", "BID", id, "修改投标 #" + id + "，报价 " + vo.quote());
        return Result.ok(vo);
    }

    /** 撤回投标 */
    @PostMapping("/bid/{id}/withdraw")
    // 撤回 / 提现
    public Result<Void> withdraw(@PathVariable Long id) {
        bidService.withdraw(SecurityUtils.userId(), id);
        // 操作留痕：撤回投标
        com.psc.witkey.service.OpLog.now("BID_WITHDRAW", "BID", id, "撤回投标 #" + id);
        return Result.ok();
    }

    /** 我的投标 */
    @GetMapping("/bid/my")
    // 我的投标
    public Result<List<BidVO>> myBids() {
        return Result.ok(bidService.myBids(SecurityUtils.userId()));
    }

    /** 雇主选标（生成担保订单） */
    @PostMapping("/bid/{id}/select")
    // 选标生成订单
    public Result<Map<String, Long>> select(@PathVariable Long id) {
        var order = bidService.select(SecurityUtils.userId(), id);
        // 操作留痕：选标
        com.psc.witkey.service.OpLog.now("BID_SELECT", "BID", id,
                "选中投标 #" + id + "，生成订单 " + order.getOrderNo());
        return Result.ok(Map.of("orderId", order.getId()));
    }

    /** 雇主查看某任务的投标列表 */
    @GetMapping("/task/{taskId}/bids")
    // 执行 listByTask
    public Result<List<BidVO>> listByTask(@PathVariable Long taskId) {
        LoginUser viewer = SecurityUtils.current();
        return Result.ok(bidService.listByTask(taskId, viewer.id(), "ADMIN".equals(viewer.role())));
    }
}
