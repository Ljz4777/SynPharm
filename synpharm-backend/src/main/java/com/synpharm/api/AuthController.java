package com.synpharm.api;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.request.SendCaptchaRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import com.synpharm.service.AuthService;
import com.synpharm.service.CaptchaService;
import com.synpharm.utils.Result;
import com.synpharm.validation.group.QqEmailLoginGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证控制器
 *
 * <p>处理用户认证相关的HTTP请求。只负责接收请求、参数校验、调用Service、返回响应，
 * 不包含任何业务逻辑（瘦控制器，胖服务）。
 *
 * <p>重要设计：登录注册合二为一
 * <ul>
 *   <li>QQ邮箱登录时，新用户自动注册，老用户直接登录</li>
 *   <li>前端只有一个登录弹窗，不用单独做注册页</li>
 *   <li>用户体验更好，不用跳来跳去</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、验证码、登出接口")
public class AuthController {

    /** 认证服务（登录总入口） */
    private final AuthService authService;

    /** 验证码服务 */
    private final CaptchaService captchaService;

    /** 验证器（用于动态分组验证） */
    private final Validator validator;

    /**
     * 登录接口
     * <p>支持多种登录方式，通过 loginType 参数区分。
     * QQ邮箱验证码登录时，新用户自动注册。
     *
     * @param request     登录请求对象
     * @param httpRequest HTTP请求对象
     * @return 登录成功返回包含accessToken和用户信息的响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持多种登录方式，QQ邮箱验证码登录时新用户自动注册")
    public Result<LoginResponse> login(@RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        // 先校验默认分组（loginType必填）
        validateByGroup(request, jakarta.validation.groups.Default.class);

        // 根据 loginType 选择对应分组进行字段验证
        switch (request.getLoginType()) {
            case "qq_email" -> validateByGroup(request, QqEmailLoginGroup.class);
            // 后续新增登录方式，在这里加对应的分组验证
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "不支持的登录方式: " + request.getLoginType());
        }

        return Result.success(authService.login(request, httpRequest));
    }

    /**
     * 使用指定分组校验请求参数
     * <p>如果校验失败，抛出 BAD_REQUEST 异常，异常处理器会统一处理。
     */
    private <T> void validateByGroup(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 发送邮箱验证码
     * <p>登录/注册都用这个接口。
     *
     * @param request 发送验证码请求对象
     * @return 发送成功返回成功响应
     */
    @PostMapping("/captcha/send")
    @Operation(summary = "发送邮箱验证码", description = "发送6位数字验证码到QQ邮箱，5分钟有效")
    public Result<Void> sendCaptcha(@Valid @RequestBody SendCaptchaRequest request) {
        captchaService.sendCaptcha(request.getEmail(), request.getType());
        return Result.success();
    }

    /**
     * 用户登出
     * <p>将当前Token加入黑名单，立即失效。
     *
     * @param request HTTP请求对象
     * @return 登出成功返回成功响应
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "将当前Token加入黑名单，立即失效")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return Result.success();
    }

    /**
     * 从请求头中提取Token
     * <p>格式：Authorization: Bearer <token>
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
