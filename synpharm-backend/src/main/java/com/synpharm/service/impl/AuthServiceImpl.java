package com.synpharm.service.impl;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_FAIL_KEY = "login:fail:";
    private static final String LOGIN_LOCK_KEY = "login:lock:";
    private static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String account = getAccount(request);
        String ip = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            checkLoginLock(account);

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
    public void resetPassword(String email, String captcha, String newPassword) {
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
            log.warn("Token加入黑名单失败: {}", e.getMessage());
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
            log.error("记录登录日志失败: {}", e.getMessage());
        }
    }
}