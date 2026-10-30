package com.study.checkin.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
