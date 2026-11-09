package com.psc.witkey.repository;

import com.psc.witkey.domain.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {

    List<TaskCategory> findByStatusOrderBySortAsc(Integer status);

    List<TaskCategory> findAllByOrderBySortAsc();

    boolean existsByName(String name);
}
