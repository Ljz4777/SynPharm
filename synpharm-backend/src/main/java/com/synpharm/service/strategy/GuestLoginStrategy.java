package com.synpharm.service.strategy;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.dto.response.UserResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.LoginStrategy;
import com.synpharm.utils.IpUtils;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestLoginStrategy implements LoginStrategy {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    /** 游客登录 IP 限流Key前缀（B-09：防止无限建号） */
    private static final String GUEST_LIMIT_KEY = "guest:limit:ip:";

    /** 每个IP每日游客注册上限 */
    private static final int GUEST_LIMIT_PER_IP = 10;

    @Override
    public String getLoginType() {
        return "guest";
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        checkGuestLimit(ip);
        String guestEmail = "guest_" + UUID.randomUUID().toString().substring(0, 8) + "@guest.local";
        String nickname = "游客_" + System.currentTimeMillis() % 10000;

        SysUser user = new SysUser();
        user.setEmail(guestEmail);
        user.setNickname(nickname);
        user.setRole("guest");
        user.setStatus(1);
        user.setEmailVerified(0);
        user.setRegisterType("guest");
        user.setLoginCount(0);
        user.setPassword(null);
        userMapper.insert(user);

        log.info("游客登录创建成功, userId: {}, email: {}", user.getId(), guestEmail);

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        updateLoginInfo(user.getId(), ip);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration() / 1000)
                .isNewUser(true)
                .user(UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .registerType(user.getRegisterType())
                        .build())
                .build();
    }

    private void updateLoginInfo(Long userId, String ip) {
        userMapper.updateLoginInfo(userId, LocalDateTime.now(), ip, LocalDateTime.now());
    }

    /**
     * 游客登录 IP 每日限流（B-09）
     *
     * <p>游客账号没有邮箱/手机号可作维度，只能按 IP 限制，防止无限建号。
     * 计数 24 小时过期，超限抛业务异常。
     */
    private void checkGuestLimit(String ip) {
        String key = GUEST_LIMIT_KEY + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
        if (count != null && count > GUEST_LIMIT_PER_IP) {
            log.warn("游客登录超过每日IP上限, ip: {}, count: {}", ip, count);
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "游客登录次数已达上限，请明天再试或注册正式账号");
        }
    }
}