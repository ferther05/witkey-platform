// WalletAccountRepository —— 数据访问：钱包账户表
package com.psc.witkey.repository;

import com.psc.witkey.domain.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 数据访问：钱包账户表。
 */
public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {

    // 按条件查询
    Optional<WalletAccount> findByUserId(Long userId);

    // 汇总托管中的冻结金额
    @Query("select coalesce(sum(w.frozen), 0) from WalletAccount w")
    // 执行 sumFrozen
    BigDecimal sumFrozen();
}
