package com.psc.witkey.service;

import com.psc.witkey.domain.OrderStatus;
import com.psc.witkey.domain.TaskStatus;
import com.psc.witkey.dto.FinanceSummaryVO;
import com.psc.witkey.repository.BizOrderRepository;
import com.psc.witkey.repository.TaskRepository;
import com.psc.witkey.repository.WalletAccountRepository;
import com.psc.witkey.repository.WalletFlowRepository;
import org.springframework.stereotype.Service;

/**
 * 平台财务口径：成交额、佣金、托管余额、充值提现总量。
 * 后台看板（模块六）直接复用这里的数字，避免两处口径不一致。
 */
@Service
public class FinanceService {

    private final BizOrderRepository orderRepository;
    private final WalletAccountRepository accountRepository;
    private final WalletFlowRepository flowRepository;
    private final TaskRepository taskRepository;

    public FinanceService(BizOrderRepository orderRepository, WalletAccountRepository accountRepository,
                          WalletFlowRepository flowRepository, TaskRepository taskRepository) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.flowRepository = flowRepository;
        this.taskRepository = taskRepository;
    }

    public FinanceSummaryVO summary() {
        var gmv = orderRepository.sumDoneAmount();
        var commission = orderRepository.sumDoneCommission();
        var escrow = accountRepository.sumFrozen();
        var recharge = flowRepository.sumAmountByType("RECHARGE");
        var withdraw = flowRepository.sumAmountByType("WITHDRAW").negate();

        long done = orderRepository.countByStatus(OrderStatus.DONE.name());
        long active = orderRepository.countByStatus(OrderStatus.TO_DELIVER.name())
                + orderRepository.countByStatus(OrderStatus.TO_ACCEPT.name());
        long pendingAudit = taskRepository.countByStatus(TaskStatus.PENDING_AUDIT.name());

        return new FinanceSummaryVO(gmv, commission, escrow, recharge, withdraw,
                done, active, pendingAudit);
    }
}
