package com.synpharm.service.impl;

import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysLoginLogMapper;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.CaptchaService;
import com.synpharm.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private CaptchaService captchaService;
    @Mock
    private SysLoginLogMapper loginLogMapper;

    private UserServiceImpl service;

    private SysUser user;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userMapper, passwordEncoder, jwtUtils, captchaService, loginLogMapper);

        user = new SysUser();
        user.setId(1L);
        user.setEmail("user@qq.com");
        user.setPassword("hashed");
        user.setStatus(1);
        user.setTokenVersion(0);
    }

    @Test
    void 修改密码_弱密码被拒绝_B17() {
        when(jwtUtils.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userMapper.selectById(1L)).thenReturn(user);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.changePassword("Bearer t", "OldPass123", "123"));
        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void 修改密码_成功后tokenVersion加一_B04() {
        when(jwtUtils.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("OldPass123", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("new-hashed");

        service.changePassword("Bearer t", "OldPass123", "NewPass123");

        assertEquals(1, user.getTokenVersion());
        assertEquals("new-hashed", user.getPassword());
        verify(userMapper).updateById(user);
    }
}
