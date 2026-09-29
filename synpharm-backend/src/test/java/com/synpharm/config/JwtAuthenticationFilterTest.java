package com.synpharm.config;

import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JWT 过滤器验人逻辑（B-04）测试。
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        filter = new JwtAuthenticationFilter(jwtUtils, redisTemplate, sysUserMapper);
        when(request.getHeader("Authorization")).thenReturn("Bearer token-1");
        when(jwtUtils.validateToken("token-1")).thenReturn(true);
        when(jwtUtils.getJtiFromToken("token-1")).thenReturn("jti-1");
        when(redisTemplate.hasKey("token:blacklist:jti-1")).thenReturn(false);
        when(jwtUtils.getUserIdFromToken("token-1")).thenReturn(1L);
        when(jwtUtils.getTokenVersionFromToken("token-1")).thenReturn(0);
        // 验人不通过的分支不会走到角色解析，用 lenient 避免严格模式报未使用桩
        lenient().when(jwtUtils.getRoleFromToken("token-1")).thenReturn("user");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().doNothing().when(filterChain).doFilter(any(), any());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private SysUser enabledUser(int tokenVersion) {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setStatus(1);
        user.setTokenVersion(tokenVersion);
        return user;
    }

    @Test
    void 用户有效_设置认证上下文() throws Exception {
        when(valueOperations.get("user:auth:1")).thenReturn(null);
        when(sysUserMapper.selectById(1L)).thenReturn(enabledUser(0));

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(1L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void token版本不符_不设置认证上下文_B04() throws Exception {
        when(valueOperations.get("user:auth:1")).thenReturn(null);
        // 用户已改密：tokenVersion 已变为 1，而 token 里还是 0
        when(sysUserMapper.selectById(1L)).thenReturn(enabledUser(1));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void 用户被禁用_不设置认证上下文_B04() throws Exception {
        when(valueOperations.get("user:auth:1")).thenReturn(null);
        SysUser disabled = enabledUser(0);
        disabled.setStatus(0);
        when(sysUserMapper.selectById(1L)).thenReturn(disabled);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void 用户不存在_不设置认证上下文() throws Exception {
        when(valueOperations.get("user:auth:1")).thenReturn(null);
        when(sysUserMapper.selectById(1L)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // 不存在的用户同样缓存，避免反复查库
        verify(valueOperations).set(eq("user:auth:1"), eq("0:0"), anyLong(), any());
    }

    @Test
    void 查库异常_failClosed不设置认证上下文() throws Exception {
        when(valueOperations.get("user:auth:1")).thenReturn(null);
        when(sysUserMapper.selectById(1L)).thenThrow(new RuntimeException("DB down"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void 缓存命中_跳过查库() throws Exception {
        when(valueOperations.get("user:auth:1")).thenReturn("1:0");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(sysUserMapper, never()).selectById(anyLong());
    }
}
