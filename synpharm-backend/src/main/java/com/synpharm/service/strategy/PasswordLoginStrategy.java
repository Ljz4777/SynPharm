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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 邮箱密码登录策略
 *
 * <p>特点：
 * <ul>
 *   <li>登录类型：password</li>
 *   <li>使用 BCrypt 校验密码（与注册/修改密码使用同一 PasswordEncoder）</li>
 *   <li>仅用于已注册（设置过密码）的用户；未注册用户提示先注册</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getLoginType() {
        return "password";
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码不能为空");
        }

        // ========== 第一步：查询用户 ==========
        SysUser user = userMapper.selectByEmail(email);
        if (user == null) {
            log.warn("密码登录-用户不存在, email: {}", email);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "该邮箱未注册，请先注册");
        }

        // ========== 第二步：检查用户状态 ==========
        if (user.getStatus() != 1) {
            log.warn("密码登录-用户已被禁用, userId: {}", user.getId());
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // ========== 第三步：校验密码 ==========
        // 验证码登录自动注册的用户 password 可能为 null，此时不能用密码登录
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.warn("密码登录-用户未设置密码, userId: {}", user.getId());
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "该账号未设置密码，请使用验证码登录或重置密码");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("密码登录-密码错误, userId: {}", user.getId());
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "密码错误");
        }

        // ========== 第四步：生成JWT Token ==========
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        String ip = IpUtils.getClientIp(httpRequest);
        userMapper.updateLoginInfo(user.getId(), LocalDateTime.now(), ip, LocalDateTime.now());

        log.info("密码登录成功, userId: {}, email: {}", user.getId(), email);
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration() / 1000)
                .isNewUser(false)
                .user(UserResponse.fromEntity(user))
                .build();
    }
}
