package com.psc.witkey.repository;

import com.psc.witkey.domain.BizOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BizOrderRepository extends JpaRepository<BizOrder, Long> {

    Optional<BizOrder> findByTaskId(Long taskId);

    Optional<BizOrder> findByOrderNo(String orderNo);

    List<BizOrder> findByEmployerIdOrderByCreatedAtDesc(Long employerId);

    List<BizOrder> findByWitkeyIdOrderByCreatedAtDesc(Long witkeyId);

    long countByWitkeyIdAndStatus(Long witkeyId, String status);

    long countByStatus(String status);

    @Query("select coalesce(sum(o.amount), 0) from BizOrder o where o.status = 'DONE'")
    BigDecimal sumDoneAmount();

    @Query("select coalesce(sum(o.commissionAmount), 0) from BizOrder o where o.status = 'DONE'")
    BigDecimal sumDoneCommission();
}
