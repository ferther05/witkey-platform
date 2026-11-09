package com.psc.witkey.dto;

import java.math.BigDecimal;

public record WalletVO(BigDecimal balance, BigDecimal frozen) {
}
