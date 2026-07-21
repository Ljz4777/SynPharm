package com.synpharm.exception;

/**
 * 错误码枚举
 * 
 * <p>定义系统中所有业务错误码。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
public enum ErrorCode {

    /** 成功 */
    SUCCESS(200, "成功"),
    
    /** 系统错误 */
    SYSTEM_ERROR(500, "系统错误"),
    
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    
    /** 未授权 */
    UNAUTHORIZED(401, "未授权"),
    
    /** 禁止访问 */
    FORBIDDEN(403, "禁止访问"),
    
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    
    /** 用户不存在 */
    USER_NOT_FOUND(1001, "用户不存在"),
    
    /** 用户已存在 */
    USER_EXISTS(1002, "用户已存在"),
    
    /** 密码错误 */
    PASSWORD_ERROR(1003, "密码错误"),
    
    /** 邮箱未验证 */
    EMAIL_NOT_VERIFIED(1004, "邮箱未验证"),
    
    /** 用户已禁用 */
    USER_DISABLED(1005, "用户已禁用"),
    
    /** Token已过期 */
    TOKEN_EXPIRED(2001, "Token已过期"),
    
    /** Token无效 */
    TOKEN_INVALID(2002, "Token无效"),
    
    /** 签名无效 */
    SIGNATURE_INVALID(2003, "签名无效"),

    /** 验证码错误或已过期 */
    CAPTCHA_ERROR(3001, "验证码错误或已过期"),

    /** 验证码发送太频繁 */
    CAPTCHA_SEND_LIMIT(3002, "验证码发送太频繁"),

    /** 任务不存在 */
    TASK_NOT_FOUND(4001, "任务不存在"),

    /** 任务正在运行 */
    TASK_RUNNING(4002, "任务正在运行"),

    /** 任务已完成 */
    TASK_COMPLETED(4003, "任务已完成"),

    /** 结果不存在 */
    RESULT_NOT_FOUND(5001, "结果不存在"),

    /** 预测失败 */
    PREDICT_ERROR(6001, "预测失败");

    /** 错误码 */
    private final int code;
    
    /** 错误消息 */
    private final String message;

    /**
     * 构造函数
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }
}