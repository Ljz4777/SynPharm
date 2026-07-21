package com.synpharm.service.impl;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysLoginLog;
import com.synpharm.repository.mapper.SysLoginLogMapper;
import com.synpharm.service.AuthService;
import com.synpharm.service.strategy.LoginStrategyFactory;
import com.synpharm.utils.IpUtils;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * <p>作为登录的总入口，负责：
 * <ol>
 *   <li>登录限流检查</li>
 *   <li>委托给具体的登录策略执行</li>
 *   <li>登录日志记录</li>
 *   <li>登出处理（Token黑名单）</li>
 * </ol>
 *
 * <p>设计原则：
 * <ul>
 *   <li>不关心具体的登录逻辑，只做公共的事情</li>
 *   <li>具体登录逻辑委托给 LoginStrategy</li>
 *   <li>新增登录方式不用改这个类</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 登录策略工厂（核心：通过策略模式支持多种登录方式） */
    private final LoginStrategyFactory loginStrategyFactory;

    /** JWT工具类 */
    private final JwtUtils jwtUtils;

    /** Redis操作 */
    private final StringRedisTemplate redisTemplate;

    /** 登录日志Mapper */
    private final SysLoginLogMapper loginLogMapper;

    /** Redis Key前缀（统一定义，避免魔法值） */
    private static final String LOGIN_FAIL_KEY = "login:fail:";
    private static final String LOGIN_LOCK_KEY = "login:lock:";
    private static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    /** 登录失败阈值 */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /** 锁定时长（分钟） */
    private static final int LOCK_DURATION_MINUTES = 15;

    /**
     * 登录总入口
     *
     * <p>流程：
     * <ol>
     *   <li>检查账户是否被锁定</li>
     *   <li>委托给策略工厂执行具体登录逻辑</li>
     *   <li>登录成功→清除失败计数</li>
     *   <li>登录失败→增加失败计数</li>
     *   <li>记录登录日志</li>
     * </ol>
     */
    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 获取登录账号（不同登录方式账号字段不同）
        String account = getAccount(request);
        String ip = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            // ========== 第一步：检查登录锁定 ==========
            checkLoginLock(account);

            // ========== 第二步：执行具体登录逻辑（策略模式） ==========
            LoginResponse response = loginStrategyFactory.login(
                    request.getLoginType(),
                    request,
                    httpRequest
            );

            // ========== 第三步：登录成功，清除失败计数 ==========
            clearLoginFailCount(account);

            // ========== 第四步：记录成功日志 ==========
            saveLoginLog(response.getUser().getId(), account, request.getLoginType(),
                    ip, userAgent, 1, null, getCaptchaType(request.getLoginType()));

            log.info("登录成功, type: {}, account: {}, ip: {}", request.getLoginType(), account, ip);
            return response;

        } catch (BusinessException e) {
            // ========== 登录失败处理 ==========
            handleLoginFail(account, ip, userAgent, request.getLoginType(), e.getMessage(),
                    getCaptchaType(request.getLoginType()));
            throw e;
        }
    }

    /**
     * 用户登出
     * <p>将Token的jti加入黑名单，验证时会检查。TTL设为Token剩余有效期，到期自动删除。
     * 使用jti而非完整Token作为Key，节省Redis内存。
     */
    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        try {
            // 从Token中解析jti作为黑名单Key（比用完整Token省内存）
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
            // Token解析失败也没关系，说明Token本来就无效
            log.warn("Token加入黑名单失败: {}", e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从登录请求中获取账号标识（不同登录方式的账号字段不同）
     *
     * @throws BusinessException 如果邮箱和手机号都为空，说明请求非法
     */
    private String getAccount(LoginRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return request.getEmail();
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            return request.getPhone();
        }
        // 无法获取账号说明请求非法，直接拒绝
        throw new BusinessException(ErrorCode.BAD_REQUEST, "无法获取登录账号");
    }

    /**
     * 根据登录类型获取验证码类型
     */
    private String getCaptchaType(String loginType) {
        return switch (loginType) {
            case "qq_email" -> "email";
            case "phone" -> "sms";
            default -> null;
        };
    }

    /**
     * 检查账户是否被锁定
     */
    private void checkLoginLock(String account) {
        String lockKey = LOGIN_LOCK_KEY + account;
        Boolean locked = redisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            Long expire = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            log.warn("账户已被锁定, account: {}, 剩余{}分钟", account, expire);
            throw new BusinessException(ErrorCode.USER_DISABLED,
                    "账户已被锁定，请" + expire + "分钟后再试");
        }
    }

    /**
     * 处理登录失败（原子递增失败计数，达到阈值则锁定）
     */
    private void handleLoginFail(String account, String ip, String userAgent,
                                 String loginType, String reason, String captchaType) {
        String failKey = LOGIN_FAIL_KEY + account;

        // 原子递增失败计数
        Long failCount = redisTemplate.opsForValue().increment(failKey);

        // 第一次失败时设置过期时间（15分钟后重置）
        if (failCount != null && failCount == 1) {
            redisTemplate.expire(failKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        // 达到阈值，锁定账户
        if (failCount != null && failCount >= MAX_LOGIN_FAIL_COUNT) {
            redisTemplate.opsForValue().set(
                    LOGIN_LOCK_KEY + account,
                    "1",
                    LOCK_DURATION_MINUTES,
                    TimeUnit.MINUTES
            );
            log.warn("账户连续登录失败{}次，已锁定, account: {}", failCount, account);
        }

        // 记录失败日志
        saveLoginLog(null, account, loginType, ip, userAgent, 0, reason, captchaType);
    }

    /**
     * 清除登录失败计数（登录成功后调用）
     */
    private void clearLoginFailCount(String account) {
        redisTemplate.delete(LOGIN_FAIL_KEY + account);
        redisTemplate.delete(LOGIN_LOCK_KEY + account);
    }

    /**
     * 记录登录日志
     * <p>记录失败不影响主流程，即使日志记录失败，登录也应该成功。
     */
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
            // 日志记录失败不影响主流程，只打印错误
            log.error("记录登录日志失败: {}", e.getMessage());
        }
    }
}
