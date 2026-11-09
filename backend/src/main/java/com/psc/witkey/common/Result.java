package com.psc.witkey.common;

/**
 * 统一响应体。code=0 表示成功，其余为业务错误码（400 参数/401 未登录/403 无权限/404 不存在/500 系统异常）。
 */
public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    public static Result<Void> ok() {
        return new Result<>(0, "ok", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
