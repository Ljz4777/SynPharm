package com.synpharm.service.strategy;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.dto.response.UserResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.CaptchaService;
import com.synpharm.service.LoginStrategy;
import com.synpharm.utils.IpUtils;
import com.synpharm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * QQ邮箱验证码登录策略
 *
 * <p>特点：
 * <ul>
 *   <li>登录注册合二为一：新用户自动注册，老用户直接登录</li>
 *   <li>只做QQ邮箱登录的逻辑，不关心其他登录方式</li>
 *   <li>验证码校验通过 CaptchaService 接口，不直接依赖具体实现</li>
 * </ul>
 *
 * <p>扩展说明：
 * <ul>
 *   <li>如果要加"图片验证码"，只需换一个 CaptchaService 实现，这里不用改</li>
 *   <li>如果要加"手机号登录"，新建一个 PhoneLoginStrategy，这里不用改</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QQEmailLoginStrategy implements LoginStrategy {

    /** 用户数据访问层 */
    private final SysUserMapper userMapper;

    /** 验证码服务（依赖接口，不依赖具体实现） */
    private final CaptchaService captchaService;

    /** JWT工具类 */
    private final JwtUtils jwtUtils;

    /**
     * 获取支持的登录类型
     */
    @Override
    public String getLoginType() {
        return "qq_email";
    }

    /**
     * 执行QQ邮箱登录
     *
     * <p>流程：
     * <ol>
     *   <li>校验验证码</li>
     *   <li>根据邮箱查用户</li>
     *   <li>用户不存在 → 自动注册新用户</li>
     *   <li>用户存在 → 检查状态</li>
     *   <li>生成JWT Token</li>
     *   <li>更新登录信息</li>
     *   <li>返回响应</li>
     * </ol>
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail();
        String captcha = request.getCaptcha();
        String ip = IpUtils.getClientIp(httpRequest);

        // ========== 第一步：校验验证码 ==========
        boolean captchaValid = captchaService.verifyCaptcha(email, captcha, "login");
        if (!captchaValid) {
            log.warn("邮箱验证码校验失败, email: {}", email);
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }
        log.info("验证码校验通过, email: {}", email);

        // ========== 第二步：查询用户 ==========
        SysUser user = userMapper.selectByEmail(email);
        boolean isNewUser = false;

        // ========== 第三步：用户不存在则自动注册 ==========
        if (user == null) {
            try {
                log.info("新用户注册, email: {}", email);
                user = registerNewUser(email);
                isNewUser = true;
            } catch (DuplicateKeyException e) {
                // 并发场景：另一个请求已经创建了该用户，直接查询即可
                log.info("并发注册冲突，重新查询用户, email: {}", email);
                user = userMapper.selectByEmail(email);
                if (user == null) {
                    // 极端情况：查询不到则抛异常
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户创建异常，请重试");
                }
            }
        } else {
            // ========== 第四步：检查用户状态 ==========
            if (user.getStatus() != 1) {
                log.warn("用户已被禁用, userId: {}, email: {}", user.getId(), email);
                throw new BusinessException(ErrorCode.USER_DISABLED);
            }
            log.info("老用户登录, userId: {}, email: {}", user.getId(), email);
        }

        // ========== 第五步：生成JWT Token ==========
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        log.debug("JWT Token生成成功, userId: {}", user.getId());

        // ========== 第六步：更新登录信息 ==========
        updateLoginInfo(user.getId(), ip);

        // ========== 第七步：返回登录响应 ==========
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration() / 1000)
                .isNewUser(isNewUser)
                .user(convertToUserResponse(user))
                .build();
    }

    /**
     * 注册新用户
     * <p>QQ邮箱登录时，用户不存在就自动创建账户。昵称默认取邮箱前缀。
     */
    private SysUser registerNewUser(String email) {
        SysUser user = new SysUser();
        user.setEmail(email);
        // 昵称默认取邮箱@前面的部分
        String nickname = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
        user.setNickname(nickname);
        user.setAvatarUrl(null);
        user.setRole("user");
        user.setStatus(1);
        user.setEmailVerified(1); // 邮箱验证码登录，邮箱天然已验证
        user.setRegisterType("qq_email");
        user.setLoginCount(0);
        user.setPassword(null); // 验证码登录不需要密码
        userMapper.insert(user);
        log.info("新用户创建成功, userId: {}, email: {}", user.getId(), email);
        return user;
    }

    /**
     * 更新用户登录信息（最后登录时间、IP、登录次数+1）
     */
    private void updateLoginInfo(Long userId, String ip) {
        userMapper.updateLoginInfo(userId, LocalDateTime.now(), ip, LocalDateTime.now());
    }

    /**
     * 实体转响应DTO（过滤敏感字段如密码）
     */
    private UserResponse convertToUserResponse(SysUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .registerType(user.getRegisterType())
                .build();
    }
}
