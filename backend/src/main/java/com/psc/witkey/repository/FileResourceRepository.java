// FileResourceRepository —— 数据访问：文件元信息表
package com.psc.witkey.repository;

import com.psc.witkey.domain.FileResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 数据访问：文件元信息表。
 */
public interface FileResourceRepository extends JpaRepository<FileResource, Long>,
        JpaSpecificationExecutor<FileResource> {

    // 按条件查询
    Page<FileResource> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);

    // 查询
    Page<FileResource> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 按条件查询
    List<FileResource> findByIdIn(List<Long> ids);

    // 按条件统计数量
    long countByBizTypeAndBizId(String bizType, Long bizId);
}
