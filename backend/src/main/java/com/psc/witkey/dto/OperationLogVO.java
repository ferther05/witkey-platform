// OperationLogVO —— 响应体：一条操作日志
package com.psc.witkey.dto;

import java.time.LocalDateTime;

/**
 * 响应体：一条操作日志。
 */
public record OperationLogVO(
        Long id,
        Long userId,
        String username,
        String role,
        String action,
        String actionText,
        String targetType,
        Long targetId,
        String detail,
        LocalDateTime createdAt
) {
}
