// NoticeRepository —— 数据访问：站内消息表
package com.psc.witkey.repository;

import com.psc.witkey.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 数据访问：站内消息表。
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 按条件查询
    Page<Notice> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 按条件统计数量
    long countByUserIdAndReadFalse(Long userId);

    // 按条件查询
    Optional<Notice> findByIdAndUserId(Long id, Long userId);

    // 全部标记已读
    @Modifying
    @Query("update Notice n set n.read = true where n.userId = :userId and n.read = false")
    // 执行 markAllRead
    int markAllRead(@Param("userId") Long userId);
}
