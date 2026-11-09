package com.psc.witkey.repository;

import com.psc.witkey.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    Optional<Notice> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("update Notice n set n.read = true where n.userId = :userId and n.read = false")
    int markAllRead(@Param("userId") Long userId);
}
