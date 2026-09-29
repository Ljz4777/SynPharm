package com.synpharm.service.impl;

import com.synpharm.dto.response.SendCaptchaResult;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.service.CaptchaService;
import com.synpharm.service.NotifyService;
import com.synpharm.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现
 *
 * <p>功能：
 * <ol>
 *   <li>生成6位随机数字验证码</li>
 *   <li>存入Redis，设置5分钟过期</li>
 *   <li>通过邮件服务发送验证码</li>
 *   <li>验证时从Redis取出比对</li>
 * </ol>
 *
 * <p>设计：
 * <ul>
 *   <li>验证码的发送通过 NotifyService 接口，不直接依赖邮件服务</li>
 *   <li>未来可以把邮箱验证码换成短信验证码，只需换个 NotifyService</li>
 *   <li>验证逻辑和发送逻辑解耦</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCaptchaServiceImpl implements CaptchaService {

    /** Redis操作 */
    private final StringRedisTemplate redisTemplate;

    /** 通知服务（依赖接口，不依赖具体实现） */
    private final NotifyService emailNotifyService;

    /** 发件邮箱（从 spring.mail.username 读取，与 JavaMailSender 共用同一配置；未配置则无法发真实邮件） */
    @Value("${spring.mail.username:}")
    private String senderEmail;

    /** 显式开发模式开关：true 时验证码直接回显给前端（仅限本地/测试，生产必须保持 false） */
    @Value("${CAPTCHA_DEV_MODE:false}")
    private boolean captchaDevMode;

    /** Redis Key前缀：验证码 */
    private static final String CAPTCHA_KEY = "captcha:email:";

    /** Redis Key前缀：发送频率限制 */
    private static final String LIMIT_KEY = "captcha:email:limit:";

    /** Redis Key前缀：60秒发送冷却（B-15） */
    private static final String COOLDOWN_KEY = "captcha:email:cooldown:";

    /** Redis Key前缀：IP 维度每小时发送上限（B-15） */
    private static final String IP_LIMIT_KEY = "captcha:email:ip:";

    /** Redis Key前缀：校验失败计数（防暴力枚举） */
    private static final String FAIL_KEY = "captcha:email:fail:";

    /** 验证码有效期（分钟） */
    private static final int CAPTCHA_EXPIRE_MINUTES = 1;

    /** 验证码长度 */
    private static final int CAPTCHA_LENGTH = 6;

    /** 每小时最多发送次数 */
    private static final int MAX_SEND_PER_HOUR = 5;

    /** 发送冷却时间（秒，B-15） */
    private static final long SEND_COOLDOWN_SECONDS = 60;

    /** 每个IP每小时最多发送次数（B-15） */
    private static final int MAX_IP_SEND_PER_HOUR = 20;

    /** 校验失败锁定阈值与时长（防止 6 位数字验证码被穷举） */
    private static final int MAX_VERIFY_FAIL_COUNT = 5;
    private static final int FAIL_LOCK_MINUTES = 10;

    /**
     * 发送邮箱验证码
     *
     * @param target 目标邮箱
     * @param type   验证码类型（login/register/reset等）
     */
    /**
     * 允许的验证码类型。
     *
     * <p>bind / change_email 供个人中心「绑定邮箱 / 换绑邮箱」使用，
     * 需与 UserServiceImpl 中的 CAPTCHA_TYPE_* 常量保持一致。
     */
    private static final Set<String> ALLOWED_TYPES =
            Set.of("login", "register", "reset", "bind", "change_email");

    @Override
    public SendCaptchaResult sendCaptcha(String target, String type) {
        // ========== 第〇步：类型白名单校验 ==========
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            log.warn("不支持的验证码类型: {}", type);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的验证码类型");
        }

        // B-16：归一化邮箱（trim + 小写），校验侧使用相同归一化，避免大写邮箱永远对不上
        target = normalizeTarget(target);

        // ========== 第一步：频率限制检查（每小时 5 次） ==========
        checkSendLimit(target);

        // ========== 第二步：60 秒冷却与 IP 维度限流（B-15） ==========
        checkCooldown(target, type);
        checkIpSendLimit();

        // ========== 第三步：生成6位随机验证码 ==========
        String code = generateCaptcha();
        log.info("生成邮箱验证码, target: {}, type: {}", target, type);

        // ========== 第四步：发送邮件（B-18：先发成功再落 Redis，发送失败不留下无效验证码） ==========
        // 显式开发模式（CAPTCHA_DEV_MODE=true）才回显验证码；生产不允许回显
        if (captchaDevMode) {
            log.warn("[开发模式] CAPTCHA_DEV_MODE=true，验证码不回发邮件。target={}, code={}", target, code);
        } else {
            if (!StringUtils.hasText(senderEmail)) {
                log.error("未配置发件邮箱 QQ_EMAIL，无法发送验证码。target={}", target);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件服务未配置，无法发送验证码");
            }

            Map<String, String> params = new HashMap<>();
            params.put("code", code);
            params.put("minutes", String.valueOf(CAPTCHA_EXPIRE_MINUTES));
            // 邮件发送失败会抛异常（NotifyService 实现负责），此时不会写入 Redis
            emailNotifyService.send(target, "captcha", params);
        }

        // ========== 第五步：邮件已发出，存入Redis并启动冷却窗口 ==========
        String key = CAPTCHA_KEY + type + ":" + target;
        redisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(COOLDOWN_KEY + type + ":" + target, "1",
                SEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);

        log.info("邮箱验证码发送成功, target: {}, type: {}", target, type);
        if (captchaDevMode) {
            return SendCaptchaResult.builder().devMode(true).code(code).build();
        }
        return SendCaptchaResult.builder().devMode(false).build();
    }

    /**
     * 验证邮箱验证码
     * <p>验证成功后删除验证码（一次性使用，防止重放攻击）。
     *
     * @param target 目标邮箱
     * @param code   用户输入的验证码
     * @param type   验证码类型
     * @return true=验证通过
     */
    @Override
    public boolean verifyCaptcha(String target, String code, String type) {
        // B-16：与发送侧相同的归一化
        target = normalizeTarget(target);

        String failKey = FAIL_KEY + type + ":" + target;

        // 防暴力枚举：失败次数达到阈值后锁定 10 分钟
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= MAX_VERIFY_FAIL_COUNT) {
            log.warn("验证码校验已锁定（失败次数过多）, target: {}", target);
            return false;
        }

        if (code == null || code.length() != CAPTCHA_LENGTH) {
            countVerifyFail(failKey, target);
            return false;
        }

        String key = CAPTCHA_KEY + type + ":" + target;
        String savedCode = redisTemplate.opsForValue().get(key);

        // 验证码不存在或已过期
        if (savedCode == null) {
            log.warn("验证码不存在或已过期, target: {}", target);
            countVerifyFail(failKey, target);
            return false;
        }

        // 验证码不匹配（使用常量时间比较，防止时序攻击）
        if (!MessageDigest.isEqual(
                savedCode.getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8))) {
            log.warn("验证码不匹配, target: {}", target);
            countVerifyFail(failKey, target);
            return false;
        }

        // 验证成功，删除验证码（一次性使用）并清空失败计数
        redisTemplate.delete(key);
        redisTemplate.delete(failKey);
        log.info("验证码验证通过, target: {}", target);
        return true;
    }

    /**
     * 校验失败计数与锁定（防止 6 位数字验证码被暴力穷举）
     */
    private void countVerifyFail(String failKey, String target) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, FAIL_LOCK_MINUTES, TimeUnit.MINUTES);
        }
        if (count != null && count >= MAX_VERIFY_FAIL_COUNT) {
            log.warn("验证码校验失败{}次，锁定{}分钟, target: {}", count, FAIL_LOCK_MINUTES, target);
        }
    }

    /**
     * 获取验证码类型
     */
    @Override
    public String getCaptchaType() {
        return "email";
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成6位数字验证码
     * <p>使用 SecureRandom 保证密码学安全的随机性，防止预测攻击。
     */
    private String generateCaptcha() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 邮箱归一化（B-16）：trim + 小写，发送与校验两侧必须一致
     */
    private String normalizeTarget(String target) {
        if (target == null || target.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱不能为空");
        }
        return target.trim().toLowerCase();
    }

    /**
     * 60 秒发送冷却检查（B-15）：同一类型同一邮箱 60 秒内只允许发一次
     *
     * <p>冷却窗口在邮件发送成功后（第五步）才开始计时——发送失败不会把用户锁住。
     */
    private void checkCooldown(String target, String type) {
        String cooldownKey = COOLDOWN_KEY + type + ":" + target;
        Boolean cooling = redisTemplate.hasKey(cooldownKey);
        if (Boolean.TRUE.equals(cooling)) {
            throw new BusinessException(ErrorCode.CAPTCHA_SEND_LIMIT,
                    "发送太频繁，请60秒后再试");
        }
    }

    /**
     * IP 维度发送上限（B-15）：每 IP 每小时最多 20 次，防止换邮箱刷邮件
     */
    private void checkIpSendLimit() {
        String ip = SecurityUtils.getClientIp();
        String ipLimitKey = IP_LIMIT_KEY + ip;
        Long count = redisTemplate.opsForValue().increment(ipLimitKey);
        if (count != null && count == 1) {
            redisTemplate.expire(ipLimitKey, 1, TimeUnit.HOURS);
        }
        if (count != null && count > MAX_IP_SEND_PER_HOUR) {
            redisTemplate.opsForValue().decrement(ipLimitKey);
            log.warn("验证码发送超过IP每小时上限, ip: {}, count: {}", ip, count);
            throw new BusinessException(ErrorCode.CAPTCHA_SEND_LIMIT,
                    "发送太频繁，请1小时后再试");
        }
    }

    /**
     * 检查发送频率限制（防止恶意刷邮件）
     * <p>使用原子递增+判断的方式，避免 get-then-check 的竞态条件。
     * 如果递增后超过限制，递减回去并抛出异常。
     */
    private void checkSendLimit(String target) {
        String limitKey = LIMIT_KEY + target;
        Long count = redisTemplate.opsForValue().increment(limitKey);

        if (count != null && count == 1) {
            // 第一次发送，设置1小时过期
            redisTemplate.expire(limitKey, 1, TimeUnit.HOURS);
        }

        if (count != null && count > MAX_SEND_PER_HOUR) {
            // 超过限制，递减回去
            redisTemplate.opsForValue().decrement(limitKey);
            log.warn("邮箱验证码发送超限, target: {}, count: {}", target, count);
            throw new BusinessException(ErrorCode.CAPTCHA_SEND_LIMIT,
                    "发送太频繁，请1小时后再试");
        }
    }
}
