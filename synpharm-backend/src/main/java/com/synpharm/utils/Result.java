package com.synpharm.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一响应结果
 * 
 * <p>用于封装API接口的统一响应格式。
 * 
 * @param <T> 响应数据类型
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 响应状态码 */
    private Integer code;
    
    /** 响应消息 */
    private String message;
    
    /** 响应数据 */
    private T data;

    /**
     * 成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("成功")
                .build();
    }

    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("成功")
                .data(data)
                .build();
    }

    /**
     * 失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 失败响应（默认错误码）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}