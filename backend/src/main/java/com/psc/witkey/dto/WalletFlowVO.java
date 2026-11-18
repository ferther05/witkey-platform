// WalletFlowVO —— 响应体：资金流水（含用户名与中文类型）
package com.psc.witkey.dto;

import com.psc.witkey.domain.WalletFlow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 资金流水展示对象：在实体基础上补用户名与中文类型，供钱包页与后台财务页复用。
 */
public record WalletFlowVO(
        Long id,
        Long userId,
        String username,
        String type,
        String typeText,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String bizNo,
        String remark,
        LocalDateTime createdAt
) {

    /** 流水类型中文（前端筛选也用这份） */
    public static final Map<String, String> TYPES = new java.util.LinkedHashMap<>() {{
        put("RECHARGE", "充值");
        put("WITHDRAW", "提现");
        put("FREEZE", "冻结");
        put("UNFREEZE", "解冻");
        put("SETTLE_PAY", "托管扣划");
        put("SETTLE_IN", "结算到账");
        put("REFUND", "退款");
        put("COMMISSION", "平台佣金");
    }};

    // 执行 from
    public static WalletFlowVO from(WalletFlow f, String username) {
        return new WalletFlowVO(f.getId(), f.getUserId(), username,
                f.getType(), TYPES.getOrDefault(f.getType(), f.getType()),
                f.getAmount(), f.getBalanceAfter(), f.getBizNo(), f.getRemark(), f.getCreatedAt());
    }
}
