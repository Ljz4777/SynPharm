package com.synpharm.service.impl;

import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.service.NotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailCaptchaServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private NotifyService notifyService;

    private EmailCaptchaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailCaptchaServiceImpl(redisTemplate, notifyService);
        ReflectionTestUtils.setField(service, "senderEmail", "test@synpharm.com");
        ReflectionTestUtils.setField(service, "captchaDevMode", false);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 发送成功_验证码写入Redis且键为归一化邮箱_B16() {
        // 频率限制：第一次发送
        when(valueOperations.increment(anyString())).thenReturn(1L);
        // 冷却检查未命中
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        service.sendCaptcha("  User@QQ.COM  ", "login");

        // B-16：Redis key 使用 trim+lowercase 后的邮箱
        verify(valueOperations).set(startsWith("captcha:email:login:user@qq.com"),
                anyString(), anyLong(), any());
        verify(notifyService).send(eq("user@qq.com"), eq("captcha"), anyMap());
    }

    @Test
    void 冷却期内发送_被拒绝_B15() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.hasKey(startsWith("captcha:email:cooldown:"))).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.sendCaptcha("user@qq.com", "login"));
        assertEquals(ErrorCode.CAPTCHA_SEND_LIMIT, e.getErrorCode());
        verify(notifyService, never()).send(anyString(), anyString(), anyMap());
    }

    @Test
    void IP每小时超过20次_被拒绝_B15() {
        when(valueOperations.increment(startsWith("captcha:email:limit:"))).thenReturn(1L);
        when(redisTemplate.hasKey(startsWith("captcha:email:cooldown:"))).thenReturn(false);
        // IP 维度计数超限（无请求上下文时 IP 为 "unknown"）
        when(valueOperations.increment(startsWith("captcha:email:ip:"))).thenReturn(21L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.sendCaptcha("user@qq.com", "login"));
        assertEquals(ErrorCode.CAPTCHA_SEND_LIMIT, e.getErrorCode());
    }

    @Test
    void 邮件发送失败_验证码不写入Redis_B18() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.hasKey(startsWith("captcha:email:cooldown:"))).thenReturn(false);
        doThrow(new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件发送失败，请稍后重试"))
                .when(notifyService).send(anyString(), anyString(), anyMap());

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.sendCaptcha("user@qq.com", "login"));
        assertEquals(ErrorCode.SYSTEM_ERROR, e.getErrorCode());
        // B-18：先发后写，发送失败不落验证码
        verify(valueOperations, never()).set(startsWith("captcha:email:login:"),
                anyString(), anyLong(), any());
    }

    @Test
    void 校验失败5次_锁定10分钟() {
        // 首次失败：fail key 不存在
        when(valueOperations.get(startsWith("captcha:email:fail:"))).thenReturn(null);
        when(valueOperations.get(startsWith("captcha:email:login:"))).thenReturn(null);
        when(valueOperations.increment(startsWith("captcha:email:fail:"))).thenReturn(5L);

        assertFalse(service.verifyCaptcha("user@qq.com", "123456", "login"));

        // 达到阈值后再校验：直接拒绝，不再读取验证码（第一次调用读过 1 次，锁定期内不再读）
        when(valueOperations.get(startsWith("captcha:email:fail:"))).thenReturn("5");
        assertFalse(service.verifyCaptcha("user@qq.com", "123456", "login"));
        verify(valueOperations, times(1)).get(startsWith("captcha:email:login:"));
    }

    @Test
    void 校验成功_删除验证码并清空失败计数() {
        when(valueOperations.get(startsWith("captcha:email:fail:"))).thenReturn(null);
        when(valueOperations.get(startsWith("captcha:email:login:"))).thenReturn("123456");

        assertTrue(service.verifyCaptcha("User@QQ.COM ", "123456", "login"));

        verify(redisTemplate).delete(startsWith("captcha:email:login:user@qq.com"));
        verify(redisTemplate).delete(startsWith("captcha:email:fail:"));
    }

    @Test
    void 发送成功_冷却窗口60秒_B15() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        service.sendCaptcha("user@qq.com", "login");

        ArgumentCaptor<Long> seconds = ArgumentCaptor.forClass(Long.class);
        verify(valueOperations).set(startsWith("captcha:email:cooldown:"),
                eq("1"), seconds.capture(), eq(TimeUnit.SECONDS));
        assertEquals(60L, seconds.getValue());
    }
}
