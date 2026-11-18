// OpLog —— 工具：操作日志静态入口
package com.psc.witkey.service;

import org.springframework.stereotype.Component;

/**
 * 操作日志的静态入口，方便在任意服务/控制器里一行记录，不用到处注入。
 */
@Component
public class OpLog {

    private static OperationLogService service;

    public OpLog(OperationLogService operationLogService) {
        OpLog.service = operationLogService;
    }

    /** 记录当前登录人的操作 */
    public static void now(String action, String targetType, Long targetId, String detail) {
        if (service != null) {
            service.recordCurrent(action, targetType, targetId, detail);
        }
    }

    /** 记录指定用户的操作（如登录、注册这类还没有上下文的场景） */
    public static void of(Long userId, String action, String targetType, Long targetId, String detail) {
        if (service != null) {
            service.record(userId, action, targetType, targetId, detail);
        }
    }
}
