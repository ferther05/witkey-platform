// BusinessException —— 业务异常：携带错误码与提示信息，由全局异常处理器统一转成 JSON
package com.psc.witkey.common;

/**
 * 业务异常：只表达"用户做错了什么"，不打印堆栈。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    // 获取Code
    public int getCode() {
        return code;
    }
}
