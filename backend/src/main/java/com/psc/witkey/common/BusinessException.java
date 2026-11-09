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

    public int getCode() {
        return code;
    }
}
