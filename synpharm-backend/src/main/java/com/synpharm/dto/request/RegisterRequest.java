package com.synpharm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.synpharm.dto.request.LoginRequest.PASSWORD_REGEX;
import static com.synpharm.dto.request.LoginRequest.QQ_EMAIL_REGEX;

/**
 * 注册请求DTO
 *
 * <p>用于接收用户注册请求的参数。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
public class RegisterRequest {

    /** QQ邮箱（必须以@qq.com结尾） */
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = QQ_EMAIL_REGEX, message = "请输入正确的QQ邮箱（如12345678@qq.com）")
    private String email;

    /** 用户密码（至少8位，必须包含大小写字母和数字） */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = PASSWORD_REGEX, message = "密码至少8位，必须包含大小写字母和数字")
    private String password;

    /** 用户昵称（2-20位中文、字母、数字、下划线） */
    @NotBlank(message = "昵称不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{2,20}$", message = "昵称长度2-20位，支持中文、字母、数字、下划线")
    private String nickname;
}