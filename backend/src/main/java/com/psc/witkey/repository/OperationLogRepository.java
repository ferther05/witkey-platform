// OperationLogRepository —— 数据访问：操作日志表
package com.psc.witkey.repository;

import com.psc.witkey.domain.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 数据访问：操作日志表（支持条件分页查询）。
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>,
        JpaSpecificationExecutor<OperationLog> {
}
