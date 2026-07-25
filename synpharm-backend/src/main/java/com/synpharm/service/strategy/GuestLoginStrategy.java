package com.synpharm.service.strategy;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.dto.response.UserResponse;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.LoginStrategy;
import com.synpharm.utils.IpUtils;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestLoginStrategy implements LoginStrategy {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Override
    public String getLoginType() {
        return "guest";
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
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

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
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
}