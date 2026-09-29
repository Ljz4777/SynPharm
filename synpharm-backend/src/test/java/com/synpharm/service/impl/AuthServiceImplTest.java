package com.synpharm.service.impl;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.repository.mapper.SysLoginLogMapper;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.CaptchaService;
import com.synpharm.service.strategy.LoginStrategyFactory;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private LoginStrategyFactory loginStrategyFactory;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SysLoginLogMapper loginLogMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private CaptchaService captchaService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private HttpServletRequest httpRequest;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(loginStrategyFactory, jwtUtils, redisTemplate,
                loginLogMapper, userMapper, captchaService, passwordEncoder);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        lenient().when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn("test-agent");
    }

    @Test
    void 登出_黑名单写入失败_抛SYSTEM_ERROR_B05() {
        when(jwtUtils.getJtiFromToken("Bearer t")).thenReturn("jti-1");
        when(jwtUtils.getRemainingTime("Bearer t")).thenReturn(60000L);
        doThrow(new RuntimeException("Redis down"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.logout("Bearer t"));
        assertEquals(ErrorCode.SYSTEM_ERROR, e.getErrorCode());
    }

    @Test
    void 登出_正常写入黑名单() {
        when(jwtUtils.getJtiFromToken("Bearer t")).thenReturn("jti-1");
        when(jwtUtils.getRemainingTime("Bearer t")).thenReturn(60000L);

        service.logout("Bearer t");

        verify(valueOperations).set(eq("token:blacklist:jti-1"), eq("1"), eq(60000L), any());
    }

    @Test
    void 登录失败_IP维度计数_B08() {
        LoginRequest request = new LoginRequest();
        request.setLoginType("password");
        request.setEmail("user@qq.com");
        request.setPassword("wrong");

        // 账号/IP 均未锁定
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        // 登录失败（密码错误）
        when(loginStrategyFactory.login(anyString(), any(), any()))
                .thenThrow(new BusinessException(ErrorCode.PASSWORD_ERROR, "邮箱或密码错误"));
        // 失败计数从 1 开始
        when(valueOperations.increment(anyString())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.login(request, httpRequest));

        // IP 维度与账号维度都计数
        verify(valueOperations, atLeastOnce()).increment(startsWith("login:fail:ip:127.0.0.1"));
        verify(valueOperations).increment(startsWith("login:fail:user@qq.com"));
    }

    @Test
    void 登录时IP已被锁定_直接拒绝_B08() {
        LoginRequest request = new LoginRequest();
        request.setLoginType("password");
        request.setEmail("user@qq.com");
        request.setPassword("wrong");

        when(redisTemplate.hasKey(startsWith("login:lock:user@qq.com"))).thenReturn(false);
        when(redisTemplate.hasKey(startsWith("login:lock:ip:127.0.0.1"))).thenReturn(true);
        when(redisTemplate.getExpire(anyString(), any())).thenReturn(5L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.login(request, httpRequest));
        assertEquals(ErrorCode.UNAUTHORIZED, e.getErrorCode());
    }
}
