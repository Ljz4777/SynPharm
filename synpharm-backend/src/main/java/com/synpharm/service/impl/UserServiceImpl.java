package com.synpharm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.synpharm.dto.response.LoginLogResponse;
import com.synpharm.dto.response.UserResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.model.entity.SysLoginLog;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysLoginLogMapper;
import com.synpharm.repository.mapper.SysUserMapper;
import com.synpharm.service.CaptchaService;
import com.synpharm.service.UserService;
import com.synpharm.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

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

    /** ORCID iD 格式：4-4-4-4（末位可为 X） */
    private static final Pattern ORCID_PATTERN = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{3}[\\dXx]$");

    /** 登录记录单次查询条数上限，防止一次拉取过多 */
    private static final int MAX_LOGIN_LOG_LIMIT = 100;

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CaptchaService captchaService;
    private final SysLoginLogMapper loginLogMapper;

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

        applyResearchProfile(userId, request);

        userMapper.updateById(user);
        log.info("更新用户资料: userId={}", userId);

        return UserResponse.fromEntity(getUserById(userId));
    }

    /**
     * 应用科研档案字段（机构 / 实验室 / ORCID / 研究方向）。
     *
     * <p>这 4 个字段按「一组」提交：只要请求中出现任意一个，就整体覆盖，
     * 未提交的视为清空。这样用户才能删除已填写的内容。
     *
     * <p>之所以不用 {@code updateById}：MyBatis-Plus 默认的 NOT_NULL 策略会跳过
     * null 值，导致「清空字段」永远不会生效，因此这里改用显式 set。
     */
    private void applyResearchProfile(Long userId, UserResponse request) {
        boolean anyPresent = request.getInstitution() != null
                || request.getLab() != null
                || request.getOrcid() != null
                || request.getResearchArea() != null;

        if (!anyPresent) {
            return;
        }

        String orcid = trimToNull(request.getOrcid());
        if (orcid != null) {
            validateOrcid(orcid);
        }

        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getInstitution, trimToNull(request.getInstitution()))
                .set(SysUser::getLab, trimToNull(request.getLab()))
                .set(SysUser::getOrcid, orcid)
                .set(SysUser::getResearchArea, trimToNull(request.getResearchArea())));

        log.info("更新科研档案: userId={}, orcid={}", userId, orcid);
    }

    /**
     * 校验 ORCID iD 的格式与校验位（ISO 7064 MOD 11-2）。
     *
     * <p>只做格式与校验位校验，不联网核实归属关系——校验位足以拦住绝大多数误填。
     * ORCID 是生物信息领域通行的研究者标识，填错会导致协作与引用无法关联。
     */
    private void validateOrcid(String orcid) {
        if (!ORCID_PATTERN.matcher(orcid).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ORCID 格式不正确，应为 0000-0000-0000-0000");
        }

        String digits = orcid.replace("-", "");
        int total = 0;
        for (int i = 0; i < 15; i++) {
            total = (total + (digits.charAt(i) - '0')) * 2;
        }
        int check = (12 - total % 11) % 11;
        char expected = check == 10 ? 'X' : (char) ('0' + check);

        if (Character.toUpperCase(digits.charAt(15)) != expected) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ORCID 校验位不正确，请核对后重试");
        }
    }

    /** 去空白并归一化：空白串 → null，便于「清空字段」语义统一 */
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
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

    // ==================== 邮箱绑定 / 换绑 ====================
    //
    // 验证码类型需与 EmailCaptchaServiceImpl 的 ALLOWED_TYPES 保持一致。

    /** 验证码类型：首次绑定邮箱 */
    private static final String CAPTCHA_TYPE_BIND = "bind";

    /** 验证码类型：换绑邮箱 */
    private static final String CAPTCHA_TYPE_CHANGE_EMAIL = "change_email";

    @Override
    @Transactional
    public UserResponse bindEmail(String token, String email, String code) {
        Long userId = getUserIdFromToken(token);
        SysUser user = getUserById(userId);

        String normalized = normalizeEmail(email);

        if (StringUtils.hasText(user.getEmail())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "账号已绑定邮箱，请使用换绑功能");
        }
        // 邮箱有唯一约束，提前校验以给出明确提示，而不是抛出数据库唯一键异常
        if (userMapper.selectByEmail(normalized) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该邮箱已被其他账号使用");
        }
        if (!captchaService.verifyCaptcha(normalized, code, CAPTCHA_TYPE_BIND)) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }

        user.setEmail(normalized);
        user.setEmailVerified(1);
        userMapper.updateById(user);

        log.info("绑定邮箱成功: userId={}", userId);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse changeEmail(String token, String newEmail, String code, String currentPassword) {
        Long userId = getUserIdFromToken(token);
        SysUser user = getUserById(userId);

        String normalized = normalizeEmail(newEmail);

        // 换绑必须同时校验当前密码：否则账号被盗后可直接换绑邮箱，进而彻底接管账号
        if (StringUtils.hasText(user.getPassword())
                && !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        if (normalized.equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新邮箱与当前邮箱相同");
        }
        if (userMapper.selectByEmail(normalized) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该邮箱已被其他账号使用");
        }
        if (!captchaService.verifyCaptcha(normalized, code, CAPTCHA_TYPE_CHANGE_EMAIL)) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }

        user.setEmail(normalized);
        user.setEmailVerified(1);
        userMapper.updateById(user);

        log.info("换绑邮箱成功: userId={}", userId);
        return UserResponse.fromEntity(user);
    }

    // ==================== 登录记录 ====================

    @Override
    public List<LoginLogResponse> getLoginLogs(String token, int limit) {
        Long userId = getUserIdFromToken(token);
        int size = Math.min(Math.max(limit, 1), MAX_LOGIN_LOG_LIMIT);

        List<SysLoginLog> logs = loginLogMapper.selectList(
                new LambdaQueryWrapper<SysLoginLog>()
                        .eq(SysLoginLog::getUserId, userId)
                        .orderByDesc(SysLoginLog::getId)
                        .last("LIMIT " + size));

        return logs.stream().map(LoginLogResponse::fromEntity).toList();
    }

    /** 邮箱归一化：去空白 + 转小写，避免「A@x.com」与「a@x.com」被当成两个账号 */
    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱不能为空");
        }
        return email.trim().toLowerCase();
    }
}
