package com.psc.witkey.service;

import com.psc.witkey.common.BusinessException;
import com.psc.witkey.common.PageResult;
import com.psc.witkey.domain.WalletAccount;
import com.psc.witkey.domain.WalletFlow;
import com.psc.witkey.dto.WalletVO;
import com.psc.witkey.repository.WalletAccountRepository;
import com.psc.witkey.repository.WalletFlowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 钱包：充值、冻结、解冻。
 * 规则：任何余额变动都必须写一条流水，流水的 bizNo 唯一，用来防重复扣款。
 * （结算与提现在模块四补齐，这里先把"发布任务冻结赏金"这条链路打通。）
 */
@Service
public class WalletService {

    private final WalletAccountRepository accountRepository;
    private final WalletFlowRepository flowRepository;

    public WalletService(WalletAccountRepository accountRepository, WalletFlowRepository flowRepository) {
        this.accountRepository = accountRepository;
        this.flowRepository = flowRepository;
    }

    public WalletAccount getOrCreate(Long userId) {
        return accountRepository.findByUserId(userId).orElseGet(() -> {
            WalletAccount account = new WalletAccount();
            account.setUserId(userId);
            account.setBalance(BigDecimal.ZERO);
            account.setFrozen(BigDecimal.ZERO);
            return accountRepository.save(account);
        });
    }

    public WalletVO info(Long userId) {
        WalletAccount account = getOrCreate(userId);
        return new WalletVO(account.getBalance(), account.getFrozen());
    }

    public PageResult<WalletFlow> flows(Long userId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        Page<WalletFlow> result = flowRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(p - 1, s));
        return new PageResult<>(result.getContent(), result.getTotalElements(), p, s);
    }

    @Transactional
    public WalletAccount recharge(Long userId, BigDecimal amount) {
        WalletAccount account = getOrCreate(userId);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        writeFlow(account, "RECHARGE", amount, "unique", "模拟充值");
        return account;
    }

    /**
     * 冻结：可用余额 -> 冻结余额。余额不足直接抛业务异常，由调用方事务回滚。
     */
    @Transactional
    public void freeze(Long userId, BigDecimal amount, String bizNo, String remark) {
        if (flowRepository.existsByBizNo(bizNo)) {
            throw new BusinessException("重复的冻结请求");
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足，请先充值（当前可用 " + account.getBalance() + " 元）");
        }
        account.setBalance(account.getBalance().subtract(amount));
        account.setFrozen(account.getFrozen().add(amount));
        accountRepository.save(account);
        writeFlow(account, "FREEZE", amount.negate(), bizNo, remark);
    }

    /**
     * 解冻：冻结余额 -> 可用余额。
     */
    @Transactional
    public void unfreeze(Long userId, BigDecimal amount, String bizNo, String remark) {
        if (flowRepository.existsByBizNo(bizNo)) {
            throw new BusinessException("重复的解冻请求");
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getFrozen().compareTo(amount) < 0) {
            throw new BusinessException("可解冻金额不足");
        }
        account.setFrozen(account.getFrozen().subtract(amount));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        writeFlow(account, "UNFREEZE", amount, bizNo, remark);
    }

    /**
     * 模拟提现：演示环境即时到账，只扣余额 + 记一条流水，不做提现审核。
     */
    @Transactional
    public WalletAccount withdraw(Long userId, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            throw new BusinessException("单次提现不超过 5 万元");
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("可提现余额不足（当前可用 " + account.getBalance() + " 元）");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        writeFlow(account, "WITHDRAW", amount.negate(), "unique", "模拟提现，演示环境即时到账");
        return account;
    }

    /**
     * 对账：用流水反推余额与冻结，和账户字段比对。
     * 这是"钱不能凭空多/少"的自检，正常永远平账。
     */
    public com.psc.witkey.dto.ReconcileVO reconcile(Long userId) {
        WalletAccount account = getOrCreate(userId);
        BigDecimal recharge = flowRepository.sumAmount(userId, java.util.List.of("RECHARGE"));
        BigDecimal unfreeze = flowRepository.sumAmount(userId, java.util.List.of("UNFREEZE"));
        BigDecimal settleIn = flowRepository.sumAmount(userId, java.util.List.of("SETTLE_IN"));
        BigDecimal freeze = flowRepository.sumAmount(userId, java.util.List.of("FREEZE"));      // 负数
        BigDecimal settlePay = flowRepository.sumAmount(userId, java.util.List.of("SETTLE_PAY")); // 负数
        BigDecimal withdraw = flowRepository.sumAmount(userId, java.util.List.of("WITHDRAW"));   // 负数

        BigDecimal computedBalance = recharge.add(unfreeze).add(settleIn).add(freeze).add(withdraw);
        // frozen = 冻结 + 结算扣划(负数，相当于减少) - 解冻
        BigDecimal computedFrozen = freeze.negate().add(settlePay).subtract(unfreeze);
        boolean consistent = computedBalance.compareTo(account.getBalance()) == 0
                && computedFrozen.compareTo(account.getFrozen()) == 0;

        return new com.psc.witkey.dto.ReconcileVO(
                account.getBalance(), account.getFrozen(),
                computedBalance, computedFrozen, consistent,
                recharge, settleIn, withdraw.negate());
    }

    /**
     * 结算：雇主的托管金额扣划，威客拿到扣除佣金后的金额。
     * bizNo 幂等，重复调用直接跳过，防止重复付款。
     */
    @Transactional
    public void settle(Long employerId, Long witkeyId, BigDecimal gross, BigDecimal fee,
                       String bizNo, String remark) {
        if (flowRepository.existsByBizNo(bizNo)) {
            return;
        }
        WalletAccount employer = getOrCreate(employerId);
        if (employer.getFrozen().compareTo(gross) < 0) {
            throw new BusinessException("托管金额不足，无法结算");
        }
        employer.setFrozen(employer.getFrozen().subtract(gross));
        accountRepository.save(employer);
        writeFlow(employer, "SETTLE_PAY", gross.negate(), bizNo + "-OUT", remark);

        WalletAccount witkey = getOrCreate(witkeyId);
        BigDecimal net = gross.subtract(fee);
        witkey.setBalance(witkey.getBalance().add(net));
        accountRepository.save(witkey);
        writeFlow(witkey, "SETTLE_IN", net, bizNo + "-IN",
                remark + "（已扣平台佣金 " + fee.stripTrailingZeros().toPlainString() + " 元）");
    }

    private void writeFlow(WalletAccount account, String type, BigDecimal amount, String bizNo, String remark) {
        String finalNo = bizNo;
        if ("unique".equals(bizNo)) {
            finalNo = type + "-" + account.getUserId() + "-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        }
        WalletFlow flow = new WalletFlow();
        flow.setUserId(account.getUserId());
        flow.setBizNo(finalNo);
        flow.setType(type);
        flow.setAmount(amount);
        flow.setBalanceAfter(account.getBalance());
        flow.setRemark(remark);
        flowRepository.save(flow);
    }
}
