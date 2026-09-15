package com.synpharm.api;

import com.synpharm.dto.response.LoginLogResponse;
import com.synpharm.dto.response.UserResponse;
import com.synpharm.service.UserService;
import com.synpharm.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * 
 * <p>处理用户信息管理相关的HTTP请求，包括获取用户信息、更新用户信息、
 * 修改密码、删除账号等功能。
 * 
 * @author SynPharm Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理接口")
public class UserController {

    /** 用户服务，处理用户相关业务逻辑 */
    private final UserService userService;

    /**
     * 获取用户信息接口
     * 
     * <p>根据Token获取当前登录用户的详细信息。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @return 用户信息响应
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserResponse> getProfile(@RequestHeader("Authorization") String token) {
        return Result.success(userService.getProfile(token));
    }

    /**
     * 更新用户信息接口
     * 
     * <p>更新当前登录用户的基本信息（昵称、头像等）。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param request 用户信息更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新用户昵称、头像等基本信息")
    public Result<UserResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserResponse request) {
        return Result.success(userService.updateProfile(token, request));
    }

    /**
     * 修改密码接口
     * 
     * <p>用户修改登录密码，需要验证旧密码。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功返回成功响应
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改用户登录密码")
    public Result<Void> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.changePassword(token, oldPassword, newPassword);
        return Result.success();
    }

    /**
     * 删除账号接口
     * 
     * <p>用户删除自己的账号，需要验证密码。
     * 
     * @param token 请求头中的JWT令牌（Bearer格式）
     * @param password 用户密码
     * @return 删除成功返回成功响应
     */
    @DeleteMapping("/account")
    @Operation(summary = "删除账号", description = "删除用户账号")
    public Result<Void> deleteAccount(
            @RequestHeader("Authorization") String token,
            @RequestParam String password) {
        userService.deleteAccount(token, password);
        return Result.success();
    }

    /**
     * 绑定邮箱接口
     *
     * <p>用于账号尚未绑定邮箱的场景。验证码需先通过
     * {@code POST /api/auth/captcha/send}（type=bind）发送到待绑定的邮箱。
     *
     * @param token JWT令牌（Bearer格式）
     * @param email 待绑定的邮箱
     * @param code  该邮箱收到的验证码
     * @return 更新后的用户信息
     */
    @PostMapping("/email")
    @Operation(summary = "绑定邮箱", description = "首次绑定邮箱，需校验新邮箱收到的验证码")
    public Result<UserResponse> bindEmail(
            @RequestHeader("Authorization") String token,
            @RequestParam String email,
            @RequestParam String code) {
        return Result.success(userService.bindEmail(token, email, code));
    }

    /**
     * 换绑邮箱接口
     *
     * <p>需同时校验当前密码与新邮箱验证码：只校验验证码不足以防止
     * 「账号被盗后直接换绑邮箱进而接管账号」。
     *
     * @param token           JWT令牌（Bearer格式）
     * @param newEmail        新邮箱
     * @param code            新邮箱收到的验证码
     * @param currentPassword 当前密码
     * @return 更新后的用户信息
     */
    @PutMapping("/email")
    @Operation(summary = "换绑邮箱", description = "更换已绑定邮箱，需校验当前密码与新邮箱验证码")
    public Result<UserResponse> changeEmail(
            @RequestHeader("Authorization") String token,
            @RequestParam String newEmail,
            @RequestParam String code,
            @RequestParam String currentPassword) {
        return Result.success(userService.changeEmail(token, newEmail, code, currentPassword));
    }

    /**
     * 登录记录接口
     *
     * <p>读取 sys_login_log 中当前用户的登录历史，用于安全审计。
     * 仅查询，不含「踢出会话」能力（那需要会话管理，见技术方案文档）。
     *
     * @param token JWT令牌（Bearer格式）
     * @param limit 返回条数上限，默认 20，最大 100
     * @return 登录记录列表（按时间倒序）
     */
    @GetMapping("/login-logs")
    @Operation(summary = "登录记录", description = "查询当前用户的登录历史，用于安全审计")
    public Result<List<LoginLogResponse>> getLoginLogs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(userService.getLoginLogs(token, limit));
    }
}