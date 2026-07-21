package com.synpharm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.synpharm.dto.request.LoginRequest.QQ_EMAIL_REGEX;

/**
 * 发送验证码请求DTO
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
public class SendCaptchaRequest {

    /** 目标邮箱（必须为QQ邮箱） */
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = QQ_EMAIL_REGEX, message = "请输入正确的QQ邮箱（如12345678@qq.com）")
    private String email;

    /** 验证码类型：login=登录注册通用 */
    @NotBlank(message = "验证码类型不能为空")
    private String type;
}
