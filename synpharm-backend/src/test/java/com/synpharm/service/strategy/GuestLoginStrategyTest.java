package com.synpharm.service.strategy;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestLoginStrategyTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private HttpServletRequest httpRequest;

    private GuestLoginStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new GuestLoginStrategy(userMapper, jwtUtils, redisTemplate);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        lenient().when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn("test-agent");
    }

    @Test
    void 游客登录超过每日IP上限_被拒绝_B09() {
        when(valueOperations.increment(startsWith("guest:limit:ip:"))).thenReturn(11L);

        LoginRequest request = new LoginRequest();
        request.setLoginType("guest");

        BusinessException e = assertThrows(BusinessException.class,
                () -> strategy.login(request, httpRequest));
        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
        verify(userMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void 游客登录正常_创建用户并签发token() {
        when(valueOperations.increment(startsWith("guest:limit:ip:"))).thenReturn(1L);
        doAnswer(inv -> {
            SysUser u = inv.getArgument(0);
            u.setId(100L);
            return 1;
        }).when(userMapper).insert(any(SysUser.class));
        when(jwtUtils.generateToken(anyLong(), anyString(), anyString(), any())).thenReturn("token-1");

        LoginRequest request = new LoginRequest();
        request.setLoginType("guest");

        var response = strategy.login(request, httpRequest);

        assertTrue(response.getIsNewUser());
        assertEquals("token-1", response.getAccessToken());
        assertEquals("guest", response.getUser().getRole());
        verify(userMapper).updateLoginInfo(eq(100L), any(), any(), any());
    }
}
