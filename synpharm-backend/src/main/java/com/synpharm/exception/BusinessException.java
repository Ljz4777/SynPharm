package com.synpharm.exception;

/**
 * 业务异常
 * 
 * <p>用于封装业务逻辑中的异常情况，包含错误码和错误消息。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final ErrorCode errorCode;

    /**
     * 构造函数
     * 
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数（自定义消息）
     * 
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     * 
     * @return ErrorCode 错误码枚举
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}