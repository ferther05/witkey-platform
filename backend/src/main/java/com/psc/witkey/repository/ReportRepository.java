package com.psc.witkey.repository;

import com.psc.witkey.domain.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByStatus(String status);
}
