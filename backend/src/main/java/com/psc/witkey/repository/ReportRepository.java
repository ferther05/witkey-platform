// ReportRepository —— 数据访问：举报表
package com.psc.witkey.repository;

import com.psc.witkey.domain.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 数据访问：举报表。
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 按条件查询
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 按条件查询
    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // 按条件统计数量
    long countByStatus(String status);
}
