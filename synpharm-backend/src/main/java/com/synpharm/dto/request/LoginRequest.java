package com.synpharm.dto.request;

import com.synpharm.validation.group.QqEmailLoginGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * <p>支持多种登录方式，通过 loginType 区分。
 * 使用分组验证（groups）实现不同登录方式的条件校验。
 *
 * @author SynPharm Team
 * @version 2.1.0
 */
@Data
public class LoginRequest {

    /** QQ邮箱正则：必须以 @qq.com 结尾，@前面为5-20位字母数字，不区分大小写 */
    public static final String QQ_EMAIL_REGEX = "^[a-zA-Z0-9]{5,20}@qq\\.com$";

    /** 密码正则：至少8位，必须包含大小写字母和数字，最大64位 */
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,64}$";

    /**
     * 登录类型（必填）
     * <ul>
     *   <li>qq_email: QQ邮箱验证码登录</li>
     *   <li>password: 邮箱密码登录（预留）</li>
     *   <li>phone: 手机号验证码登录（预留）</li>
     *   <li>wechat: 微信登录（预留）</li>
     * </ul>
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    /**
     * QQ邮箱（qq_email 登录方式必填，必须以@qq.com结尾）
     * <p>使用分组验证，只有 QqEmailLoginGroup 组才校验邮箱格式和非空。
     */
    @NotBlank(message = "邮箱不能为空", groups = QqEmailLoginGroup.class)
    @Pattern(regexp = QQ_EMAIL_REGEX, message = "请输入正确的QQ邮箱（如12345678@qq.com）",
            groups = QqEmailLoginGroup.class)
    private String email;

    /**
     * 验证码（邮箱/手机号登录必填，6位数字）
     * <p>使用分组验证，只有 QqEmailLoginGroup 组才校验验证码格式和非空。
     */
    @NotBlank(message = "验证码不能为空", groups = QqEmailLoginGroup.class)
    @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字", groups = QqEmailLoginGroup.class)
    private String captcha;

    /**
     * 密码（password 登录方式必填，至少8位，含大小写字母和数字，最大64位）
     * <p>当前未使用密码登录方式，预留字段。
     */
    @Pattern(regexp = PASSWORD_REGEX, message = "密码至少8位，必须包含大小写字母和数字")
    private String password;

    /** 手机号（phone 登录方式必填，预留） */
    private String phone;

    /** 微信授权码（wechat 登录方式必填，预留） */
    private String wechatCode;
}
