package com.psc.witkey.repository;

import com.psc.witkey.domain.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {

    Optional<WalletAccount> findByUserId(Long userId);

    @Query("select coalesce(sum(w.frozen), 0) from WalletAccount w")
    BigDecimal sumFrozen();
}
