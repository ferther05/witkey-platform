// WalletController —— 接口：钱包余额、流水、充值、提现、对账
package com.psc.witkey.controller;

import com.psc.witkey.common.PageResult;
import com.psc.witkey.common.Result;
import com.psc.witkey.domain.WalletFlow;
import com.psc.witkey.dto.RechargeRequest;
import com.psc.witkey.dto.ReconcileVO;
import com.psc.witkey.dto.WalletVO;
import com.psc.witkey.dto.WithdrawRequest;
import com.psc.witkey.security.SecurityUtils;
import com.psc.witkey.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口：钱包余额、流水、充值、提现、对账。
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // 查询钱包账户信息（没有则创建）
    @GetMapping
    // 执行 info
    public Result<WalletVO> info() {
        return Result.ok(walletService.info(SecurityUtils.userId()));
    }

    // 查询资金流水列表
    @GetMapping("/flows")
    // 我的资金流水（可按类型 / 时间筛选）
    public Result<PageResult<WalletFlow>> flows(
            @RequestParam(required = false) String type,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime start,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            java.time.LocalDateTime end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(walletService.flows(SecurityUtils.userId(), type, start, end, page, size));
    }

    /** 模拟充值：仅演示用，不接真实支付 */
    @PostMapping("/recharge")
    // 模拟充值
    public Result<WalletVO> recharge(@RequestBody @Valid RechargeRequest req) {
        var account = walletService.recharge(SecurityUtils.userId(), req.amount());
        com.psc.witkey.service.OpLog.now("WALLET_RECHARGE", "WALLET", null, "模拟充值 " + req.amount() + " 元");
        return Result.ok(new WalletVO(account.getBalance(), account.getFrozen()));
    }

    /** 模拟提现：演示环境即时到账 */
    @PostMapping("/withdraw")
    // 撤回 / 提现
    public Result<WalletVO> withdraw(@RequestBody @Valid WithdrawRequest req) {
        var account = walletService.withdraw(SecurityUtils.userId(), req.amount());
        com.psc.witkey.service.OpLog.now("WALLET_WITHDRAW", "WALLET", null, "模拟提现 " + req.amount() + " 元");
        return Result.ok(new WalletVO(account.getBalance(), account.getFrozen()));
    }

    /** 对账：用流水反推余额，校验是否平账 */
    @GetMapping("/reconcile")
    // 账户对账
    public Result<ReconcileVO> reconcile() {
        return Result.ok(walletService.reconcile(SecurityUtils.userId()));
    }
}
