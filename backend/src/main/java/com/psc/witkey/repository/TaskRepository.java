// TaskRepository —— 数据访问：任务表
package com.psc.witkey.repository;

import com.psc.witkey.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 数据访问：任务表。
 */
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    // 按条件查询
    Page<Task> findByEmployerId(Long employerId, Pageable pageable);

    // 按条件查询
    Page<Task> findByEmployerIdAndStatus(Long employerId, String status, Pageable pageable);

    // 按条件查询
    List<Task> findByStatusIn(List<String> statuses);

    // 按条件统计数量
    long countByStatus(String status);

    // 按条件统计数量
    long countByCategoryId(Long categoryId);

    // 某雇主的任务总数
    long countByEmployerId(Long employerId);

    // 有多少任务引用了该文件（附件字段里出现 id）
    @org.springframework.data.jpa.repository.Query(
            "select count(t) from Task t where t.fileIds like concat('%', :fid, '%')")
    // 按条件计数
    long countByFileRef(@org.springframework.data.repository.query.Param("fid") String fid);

    // 按条件查询
    java.util.List<Task> findByStatusAndDeadlineBefore(String status, java.time.LocalDateTime time);
}
