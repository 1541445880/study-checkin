package com.study.checkin.exception;

import com.study.checkin.common.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUpload(MaxUploadSizeExceededException e) {
        return Result.fail("图片太大啦，请选择 5MB 以内的图片");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValid(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String msg = error != null ? error.getDefaultMessage() : "参数错误";
        return Result.fail(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception e) {
        return Result.fail("服务器异常：" + e.getMessage());
    }
}
