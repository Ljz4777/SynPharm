package com.synpharm.api;

import com.synpharm.dto.response.UserResponse;
import com.synpharm.service.UserService;
import com.synpharm.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}