// GlobalExceptionHandler —— 全局异常处理：把业务异常、校验失败、未知异常统一包装成
package com.psc.witkey.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把各种异常收敛成统一的 Result 结构，前端只需判断 code。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 业务异常转统一响应
    @ExceptionHandler(BusinessException.class)
    // 执行 handleBusiness
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        HttpStatus status = switch (e.getCode()) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(Result.fail(e.getCode(), e.getMessage()));
    }

    // 参数校验失败转 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 执行 handleValidation
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return ResponseEntity.badRequest().body(Result.fail(400, message));
    }

    // 无权限访问时返回 403
    @ExceptionHandler(AccessDeniedException.class)
    // 执行 handleAccessDenied
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.fail(403, "没有操作权限"));
    }

    // 未登录时返回 401
    @ExceptionHandler(AuthenticationException.class)
    // 执行 handleAuthentication
    public ResponseEntity<Result<Void>> handleAuthentication(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.fail(401, "未登录或登录已过期"));
    }

    // 上传超限时返回 400
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    // 执行 handleUploadTooLarge
    public ResponseEntity<Result<Void>> handleUploadTooLarge(
            org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(Result.fail(400, "文件不能超过 20MB"));
    }

    // 未知异常兜底（记录日志 + 500）
    @ExceptionHandler(Exception.class)
    // 执行 handleOther
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("未处理异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, "服务器开小差了，请稍后再试"));
    }
}
