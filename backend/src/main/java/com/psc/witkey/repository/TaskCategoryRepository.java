// TaskCategoryRepository —— 数据访问：任务分类表
package com.psc.witkey.repository;

import com.psc.witkey.domain.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 数据访问：任务分类表。
 */
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {

    // 按条件查询
    List<TaskCategory> findByStatusOrderBySortAsc(Integer status);

    // 查询
    List<TaskCategory> findAllByOrderBySortAsc();

    // 分类名是否已存在
    boolean existsByName(String name);
}
