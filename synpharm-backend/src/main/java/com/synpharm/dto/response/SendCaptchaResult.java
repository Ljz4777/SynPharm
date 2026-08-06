package com.synpharm.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 发送验证码结果 DTO
 *
 * <p>devMode=true 表示未配置发件邮箱（开发/本地模式），
 * 验证码不会真实发送到邮箱，而是直接返回给前端显示，便于本地联调。
 * 生产环境配置了 QQ_EMAIL 后 devMode=false，验证码走真实邮件，code 为 null。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Data
@Builder
public class SendCaptchaResult {

    /** 是否开发模式（未配置发件邮箱 QQ_EMAIL，验证码不回发邮件） */
    private boolean devMode;

    /** 开发模式下返回的验证码（生产模式为 null） */
    private String code;
}
