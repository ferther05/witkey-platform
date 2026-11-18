// BidRepository —— 数据访问：投标表
package com.psc.witkey.repository;

import com.psc.witkey.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 数据访问：投标表。
 */
public interface BidRepository extends JpaRepository<Bid, Long> {

    // 按条件查询
    List<Bid> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    // 按条件查询
    Optional<Bid> findByTaskIdAndWitkeyId(Long taskId, Long witkeyId);

    // 按条件查询
    List<Bid> findByWitkeyIdOrderByCreatedAtDesc(Long witkeyId);

    // 按条件统计数量
    long countByTaskIdAndStatus(Long taskId, String status);

    // 按条件统计数量
    long countByTaskId(Long taskId);

    // 某威客的投标总数
    long countByWitkeyId(Long witkeyId);
}
