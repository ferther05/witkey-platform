// TaskFavoriteRepository —— 数据访问：任务收藏表
package com.psc.witkey.repository;

import com.psc.witkey.domain.TaskFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 数据访问：任务收藏表。
 */
public interface TaskFavoriteRepository extends JpaRepository<TaskFavorite, Long> {

    // 按条件查询
    Optional<TaskFavorite> findByUserIdAndTaskId(Long userId, Long taskId);

    // 是否已收藏该任务
    boolean existsByUserIdAndTaskId(Long userId, Long taskId);

    // 按条件查询
    Page<TaskFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 按条件查询
    List<TaskFavorite> findByUserIdAndTaskIdIn(Long userId, List<Long> taskIds);
}
