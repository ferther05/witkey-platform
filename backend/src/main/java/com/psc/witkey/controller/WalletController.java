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

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public Result<WalletVO> info() {
        return Result.ok(walletService.info(SecurityUtils.userId()));
    }

    @GetMapping("/flows")
    public Result<PageResult<WalletFlow>> flows(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(walletService.flows(SecurityUtils.userId(), page, size));
    }

    /** 模拟充值：仅演示用，不接真实支付 */
    @PostMapping("/recharge")
    public Result<WalletVO> recharge(@RequestBody @Valid RechargeRequest req) {
        var account = walletService.recharge(SecurityUtils.userId(), req.amount());
        return Result.ok(new WalletVO(account.getBalance(), account.getFrozen()));
    }

    /** 模拟提现：演示环境即时到账 */
    @PostMapping("/withdraw")
    public Result<WalletVO> withdraw(@RequestBody @Valid WithdrawRequest req) {
        var account = walletService.withdraw(SecurityUtils.userId(), req.amount());
        return Result.ok(new WalletVO(account.getBalance(), account.getFrozen()));
    }

    /** 对账：用流水反推余额，校验是否平账 */
    @GetMapping("/reconcile")
    public Result<ReconcileVO> reconcile() {
        return Result.ok(walletService.reconcile(SecurityUtils.userId()));
    }
}
