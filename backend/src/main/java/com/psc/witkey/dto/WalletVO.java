// WalletVO —— 响应体：钱包余额与冻结金额
package com.psc.witkey.dto;

import java.math.BigDecimal;

/**
 * 响应体：钱包余额与冻结金额。
 */
public record WalletVO(BigDecimal balance, BigDecimal frozen) {
}
