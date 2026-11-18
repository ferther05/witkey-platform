// BizOrderRepository —— 数据访问：担保订单表
package com.psc.witkey.repository;

import com.psc.witkey.domain.BizOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 数据访问：担保订单表。
 */
public interface BizOrderRepository extends JpaRepository<BizOrder, Long> {

    // 按条件查询
    Optional<BizOrder> findByTaskId(Long taskId);

    // 按条件查询
    Optional<BizOrder> findByOrderNo(String orderNo);

    // 按条件查询
    List<BizOrder> findByEmployerIdOrderByCreatedAtDesc(Long employerId);

    // 按条件查询
    List<BizOrder> findByWitkeyIdOrderByCreatedAtDesc(Long witkeyId);

    // 按条件统计数量
    long countByWitkeyIdAndStatus(Long witkeyId, String status);

    // 按条件统计数量
    long countByEmployerIdAndStatus(Long employerId, String status);

    // 按条件统计数量
    long countByStatus(String status);

    // 某雇主 / 某威客的订单总数
    long countByEmployerId(Long employerId);

    // 有多少订单的交付附件引用了该文件
    @org.springframework.data.jpa.repository.Query(
            "select count(o) from BizOrder o where o.deliverFileIds like concat('%', :fid, '%')")
    // 按条件计数
    long countByDeliverFileRef(@org.springframework.data.repository.query.Param("fid") String fid);
    // 按条件计数
    long countByWitkeyId(Long witkeyId);

    /** 已交付且超过指定时间仍未验收的订单（超时自动验收用） */
    java.util.List<BizOrder> findByStatusAndDeliverTimeBefore(String status, java.time.LocalDateTime time);

    @Query("select coalesce(sum(o.amount), 0) from BizOrder o where o.status = 'DONE'")
    // 执行 sumDoneAmount
    BigDecimal sumDoneAmount();

    // 已完成订单的佣金总额
    @Query("select coalesce(sum(o.commissionAmount), 0) from BizOrder o where o.status = 'DONE'")
    // 执行 sumDoneCommission
    BigDecimal sumDoneCommission();
}
