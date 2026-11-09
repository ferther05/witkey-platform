package com.psc.witkey.repository;

import com.psc.witkey.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByEmployerId(Long employerId, Pageable pageable);

    Page<Task> findByEmployerIdAndStatus(Long employerId, String status, Pageable pageable);

    List<Task> findByStatusIn(List<String> statuses);

    long countByStatus(String status);

    long countByCategoryId(Long categoryId);

    java.util.List<Task> findByStatusAndDeadlineBefore(String status, java.time.LocalDateTime time);
}
