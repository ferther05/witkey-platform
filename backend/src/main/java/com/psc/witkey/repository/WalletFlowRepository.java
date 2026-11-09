package com.psc.witkey.repository;

import com.psc.witkey.domain.WalletFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;

public interface WalletFlowRepository extends JpaRepository<WalletFlow, Long> {

    Page<WalletFlow> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByBizNo(String bizNo);

    @Query("select coalesce(sum(f.amount), 0) from WalletFlow f where f.userId = :userId and f.type in :types")
    BigDecimal sumAmount(@Param("userId") Long userId, @Param("types") Collection<String> types);

    @Query("select coalesce(sum(f.amount), 0) from WalletFlow f where f.type = :type")
    BigDecimal sumAmountByType(@Param("type") String type);
}
