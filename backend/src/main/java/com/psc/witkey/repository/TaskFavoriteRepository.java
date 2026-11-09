package com.psc.witkey.repository;

import com.psc.witkey.domain.TaskFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskFavoriteRepository extends JpaRepository<TaskFavorite, Long> {

    Optional<TaskFavorite> findByUserIdAndTaskId(Long userId, Long taskId);

    boolean existsByUserIdAndTaskId(Long userId, Long taskId);

    Page<TaskFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<TaskFavorite> findByUserIdAndTaskIdIn(Long userId, List<Long> taskIds);
}
