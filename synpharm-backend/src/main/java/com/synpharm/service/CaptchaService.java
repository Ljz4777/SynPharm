package com.synpharm.service;

import com.synpharm.dto.response.SendCaptchaResult;

/**
 * 验证码服务接口
 *
 * <p>所有验证码实现都必须遵守这个契约。
 * 未来可以从邮箱验证码切换到滑块验证、极验等，不用改登录逻辑。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
public interface CaptchaService {

    /**
     * 发送验证码
     *
     * @param target 发送目标（邮箱/手机号）
     * @param type   验证码类型（注册/登录/找回密码等）
     * @return 发送结果（devMode=true 时携带验证码用于本地回显）
     */
    SendCaptchaResult sendCaptcha(String target, String type);

    /**
     * 验证验证码是否正确
     * <p>验证成功后应删除验证码（一次性使用）。
     *
     * @param target 发送目标（邮箱/手机号）
     * @param code   用户输入的验证码
     * @param type   验证码类型
     * @return true=验证通过，false=验证失败
     */
    boolean verifyCaptcha(String target, String code, String type);

    /**
     * 获取验证码类型标识
     *
     * @return 验证码类型，如 "email"、"sms"
     */
    String getCaptchaType();
}
