package com.synpharm.service.impl;

import com.synpharm.dto.response.UserResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.UserService;
import com.synpharm.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 *
 * <p>实现用户信息管理相关的业务操作，包括查询、更新、密码管理等。
 * 登录/注册职责已归 AuthService，本类不再包含相关方法。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public SysUser getUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public SysUser getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, String nickname, String avatarUrl) {
        SysUser user = getUserById(userId);

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        userMapper.updateById(user);
        log.info("更新用户信息: userId={}", userId);

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        getUserById(userId);
        userMapper.deleteById(userId);
        log.info("删除用户: userId={}", userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.selectByEmail(email) != null;
    }

    @Override
    public UserResponse getProfile(String token) {
        Long userId = getUserIdFromToken(token);
        SysUser user = getUserById(userId);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String token, UserResponse request) {
        Long userId = getUserIdFromToken(token);
        SysUser user = getUserById(userId);

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userMapper.updateById(user);
        log.info("更新用户资料: userId={}", userId);

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void changePassword(String token, String oldPassword, String newPassword) {
        Long userId = getUserIdFromToken(token);
        SysUser user = getUserById(userId);

        // 验证码登录的用户可能没有设置密码，此时 oldPassword 可为空
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_ERROR);
            }
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("修改密码成功: userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteAccount(String token, String password) {
        Long userId = getUserIdFromToken(token);
        SysUser user = getUserById(userId);

        // 验证码登录的用户可能没有设置密码，此时跳过密码校验
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_ERROR);
            }
        }

        userMapper.deleteById(userId);
        log.info("删除账号: userId={}", userId);
    }

    /**
     * 从Token中提取用户ID
     */
    private Long getUserIdFromToken(String token) {
        String actualToken = extractToken(token);
        return jwtUtils.getUserIdFromToken(actualToken);
    }

    /**
     * 提取Token（移除Bearer前缀）
     */
    private String extractToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
