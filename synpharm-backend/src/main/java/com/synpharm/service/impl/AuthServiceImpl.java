package com.synpharm.service.impl;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.request.RegisterRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.dto.response.UserResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysLoginLog;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysLoginLogMapper;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.AuthService;
import com.synpharm.service.CaptchaService;
import com.synpharm.service.strategy.LoginStrategyFactory;
import com.synpharm.utils.IpUtils;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LoginStrategyFactory loginStrategyFactory;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final SysLoginLogMapper loginLogMapper;
    private final SysUserMapper userMapper;
    private final CaptchaService captchaService;
    private final PasswordEncoder passwordEncoder;

    private static final String LOGIN_FAIL_KEY = "login:fail:";
    private static final String LOGIN_LOCK_KEY = "login:lock:";
    private static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    /** IP 维度失败计数与锁定（B-08：防止仅按账号锁定被恶意触发） */
    private static final String IP_FAIL_KEY = "login:fail:ip:";
    private static final String IP_LOCK_KEY = "login:lock:ip:";

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int MAX_IP_FAIL_COUNT = 20;
    private static final int LOCK_DURATION_MINUTES = 15;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String account = getAccount(request);
        String ip = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            checkLoginLock(account);
            checkIpLock(ip);

            LoginResponse response = loginStrategyFactory.login(
                    request.getLoginType(),
                    request,
                    httpRequest
            );

            clearLoginFailCount(account);

            saveLoginLog(response.getUser().getId(), account, request.getLoginType(),
                    ip, userAgent, 1, null, getCaptchaType(request.getLoginType()));

            log.info("登录成功, type: {}, account: {}, ip: {}", request.getLoginType(), account, ip);
            return response;

        } catch (BusinessException e) {
            handleLoginFail(account, ip, userAgent, request.getLoginType(), e.getMessage(),
                    getCaptchaType(request.getLoginType()));
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail();

        // ========== 第一步：检查邮箱是否已注册 ==========
        if (userMapper.selectByEmail(email) != null) {
            log.warn("注册-邮箱已存在, email: {}", email);
            throw new BusinessException(ErrorCode.USER_EXISTS, "该邮箱已注册，请直接登录");
        }

        // ========== 第二步：校验验证码（type=register） ==========
        boolean captchaValid = captchaService.verifyCaptcha(email, request.getCaptcha(), "register");
        if (!captchaValid) {
            log.warn("注册-验证码校验失败, email: {}", email);
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码错误或已过期");
        }

        // ========== 第三步：创建用户 ==========
        SysUser user = new SysUser();
        user.setEmail(email);
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        user.setStatus(1);
        user.setEmailVerified(1);
        user.setRegisterType("qq_email");
        user.setLoginCount(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            log.warn("注册-并发冲突, email: {}", email);
            throw new BusinessException(ErrorCode.USER_EXISTS, "该邮箱已注册，请直接登录");
        }
        log.info("注册成功, userId: {}, email: {}", user.getId(), email);

        // ========== 第四步：生成Token并自动登录 ==========
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        String ip = IpUtils.getClientIp(httpRequest);
        userMapper.updateLoginInfo(user.getId(), LocalDateTime.now(), ip, LocalDateTime.now());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration() / 1000)
                .isNewUser(true)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String captcha, String newPassword) {
        if (newPassword == null || !Pattern.matches(LoginRequest.PASSWORD_REGEX, newPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码至少8位，必须包含大小写字母和数字");
        }

        boolean captchaValid = captchaService.verifyCaptcha(email, captcha, "reset");
        if (!captchaValid) {
            log.warn("忘记密码验证码校验失败, email: {}", email);
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码错误或已过期");
        }

        SysUser user = userMapper.selectByEmail(email);
        if (user == null) {
            log.warn("忘记密码-用户不存在, email: {}", email);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该邮箱未注册");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        // B-04：密码重置后 token 版本 +1，所有旧 token 立即失效
        user.setTokenVersion(user.getTokenVersion() == null ? 1 : user.getTokenVersion() + 1);
        userMapper.updateById(user);

        log.info("忘记密码-密码重置成功, email: {}", email);
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        try {
            String jti = jwtUtils.getJtiFromToken(token);
            if (jti == null) {
                return;
            }
            long remainingTime = jwtUtils.getRemainingTime(token);
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_KEY + jti,
                        "1",
                        remainingTime,
                        TimeUnit.MILLISECONDS
                );
                log.info("Token已加入黑名单, jti: {}", jti);
            }
        } catch (Exception e) {
            // B-05：黑名单写入失败不再假装成功，抛错让用户知道登出未完成
            log.error("Token加入黑名单失败, jti 写入异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出登录失败，请稍后重试");
        }
    }

    private String getAccount(LoginRequest request) {
        if ("guest".equals(request.getLoginType())) {
            return "guest";
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return request.getEmail();
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            return request.getPhone();
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "无法获取登录账号");
    }

    private String getCaptchaType(String loginType) {
        return switch (loginType) {
            case "qq_email" -> "email";
            case "phone" -> "sms";
            default -> null;
        };
    }

    private void checkLoginLock(String account) {
        if ("guest".equals(account)) {
            return;
        }
        String lockKey = LOGIN_LOCK_KEY + account;
        Boolean locked = redisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            Long expire = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            log.warn("账户已被锁定, account: {}, 剩余{}分钟", account, expire);
            throw new BusinessException(ErrorCode.USER_DISABLED,
                    "账户已被锁定，请" + expire + "分钟后再试");
        }
    }

    private void handleLoginFail(String account, String ip, String userAgent,
                                 String loginType, String reason, String captchaType) {
        // B-08：IP 维度失败计数对所有登录类型生效（含 guest），防止单 IP 暴力扫描
        countIpFail(ip);

        if ("guest".equals(account)) {
            return;
        }
        String failKey = LOGIN_FAIL_KEY + account;
        Long failCount = redisTemplate.opsForValue().increment(failKey);

        if (failCount != null && failCount == 1) {
            redisTemplate.expire(failKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        if (failCount != null && failCount >= MAX_LOGIN_FAIL_COUNT) {
            redisTemplate.opsForValue().set(
                    LOGIN_LOCK_KEY + account,
                    "1",
                    LOCK_DURATION_MINUTES,
                    TimeUnit.MINUTES
            );
            log.warn("账户连续登录失败{}次，已锁定, account: {}", failCount, account);
        }

        saveLoginLog(null, account, loginType, ip, userAgent, 0, reason, captchaType);
    }

    /**
     * IP 维度登录失败计数与锁定（B-08）
     *
     * <p>与账号维度并行：同一 IP 短时间内失败过多（如扫描他人邮箱触发锁定）
     * 时整体限流，防止攻击者用别人的账号名触发锁定影响正常用户。
     */
    private void countIpFail(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        String failKey = IP_FAIL_KEY + ip;
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        if (failCount != null && failCount == 1) {
            redisTemplate.expire(failKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        if (failCount != null && failCount >= MAX_IP_FAIL_COUNT) {
            redisTemplate.opsForValue().set(
                    IP_LOCK_KEY + ip,
                    "1",
                    LOCK_DURATION_MINUTES,
                    TimeUnit.MINUTES
            );
            log.warn("IP 登录失败次数过多，已锁定, ip: {}, count: {}", ip, failCount);
        }
    }

    /**
     * 检查 IP 是否已被登录失败锁定（B-08）
     */
    private void checkIpLock(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        Boolean locked = redisTemplate.hasKey(IP_LOCK_KEY + ip);
        if (Boolean.TRUE.equals(locked)) {
            Long expire = redisTemplate.getExpire(IP_LOCK_KEY + ip, TimeUnit.MINUTES);
            log.warn("IP已被登录失败锁定, ip: {}, 剩余{}分钟", ip, expire);
            throw new BusinessException(ErrorCode.UNAUTHORIZED,
                    "登录尝试过于频繁，请" + (expire == null ? "稍后" : expire + "分钟后") + "再试");
        }
    }

    private void clearLoginFailCount(String account) {
        if ("guest".equals(account)) {
            return;
        }
        redisTemplate.delete(LOGIN_FAIL_KEY + account);
        redisTemplate.delete(LOGIN_LOCK_KEY + account);
    }

    private void saveLoginLog(Long userId, String account, String loginType,
                              String ip, String userAgent, Integer status,
                              String failReason, String captchaType) {
        try {
            SysLoginLog logEntity = new SysLoginLog();
            logEntity.setUserId(userId);
            logEntity.setAccount(account);
            logEntity.setLoginType(loginType);
            logEntity.setLoginIp(ip);
            logEntity.setUserAgent(userAgent);
            logEntity.setCaptchaType(captchaType);
            logEntity.setStatus(status);
            logEntity.setFailReason(failReason);
            loginLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }
}