package com.synpharm.api;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.request.RegisterRequest;
import com.synpharm.dto.request.SendCaptchaRequest;
import com.synpharm.dto.response.LoginResponse;
import com.synpharm.dto.response.SendCaptchaResult;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、验证码、忘记密码、登出接口")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final Validator validator;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持多种登录方式：qq_email（QQ邮箱验证码，新用户自动注册）、guest（游客登录）")
    public Result<LoginResponse> login(@RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        validateByGroup(request, jakarta.validation.groups.Default.class);

        switch (request.getLoginType()) {
            case "qq_email" -> validateByGroup(request, QqEmailLoginGroup.class);
            case "guest" -> {
            }
            case "password" -> {
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "不支持的登录方式: " + request.getLoginType());
        }

        return Result.success(authService.login(request, httpRequest));
    }

    @PostMapping("/captcha/send")
    @Operation(summary = "发送邮箱验证码", description = "发送6位数字验证码到QQ邮箱，1分钟有效。type=login/register/reset")
    public Result<SendCaptchaResult> sendCaptcha(@Valid @RequestBody SendCaptchaRequest request) {
        return Result.success(captchaService.sendCaptcha(request.getEmail(), request.getType()));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "忘记密码", description = "使用邮箱验证码重置密码")
    public Result<Void> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        String captcha = body.get("captcha");
        String newPassword = body.get("newPassword");

        if (!StringUtils.hasText(email) || !StringUtils.hasText(captcha) || !StringUtils.hasText(newPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数不完整");
        }

        authService.resetPassword(email, captcha, newPassword);
        return Result.success();
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "将当前Token加入黑名单，立即失效")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return Result.success();
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用QQ邮箱+密码注册，验证码type=register；注册成功后自动登录")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                          HttpServletRequest httpRequest) {
        return Result.success(authService.register(request, httpRequest));
    }

    private <T> void validateByGroup(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}