// WalletFlowRepository —— 数据访问：资金流水表
package com.psc.witkey.repository;

import com.psc.witkey.domain.WalletFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * 数据访问：资金流水表。
 */
public interface WalletFlowRepository extends JpaRepository<WalletFlow, Long>,
        JpaSpecificationExecutor<WalletFlow> {

    // 按条件查询
    Page<WalletFlow> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 流水号是否已存在（幂等判断）
    boolean existsByBizNo(String bizNo);

    // 按流水类型汇总金额
    @Query("select coalesce(sum(f.amount), 0) from WalletFlow f where f.userId = :userId and f.type in :types")
    // 执行 sumAmount
    BigDecimal sumAmount(@Param("userId") Long userId, @Param("types") Collection<String> types);

    // 按类型汇总流水金额
    @Query("select coalesce(sum(f.amount), 0) from WalletFlow f where f.type = :type")
    // 执行 sumAmountByType
    BigDecimal sumAmountByType(@Param("type") String type);
}
