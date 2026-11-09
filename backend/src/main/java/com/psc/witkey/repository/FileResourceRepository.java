package com.psc.witkey.repository;

import com.psc.witkey.domain.FileResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileResourceRepository extends JpaRepository<FileResource, Long> {

    Page<FileResource> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);

    Page<FileResource> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<FileResource> findByIdIn(List<Long> ids);

    long countByBizTypeAndBizId(String bizType, Long bizId);
}
