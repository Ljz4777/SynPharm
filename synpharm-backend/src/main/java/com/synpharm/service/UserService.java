package com.synpharm.service;

import com.synpharm.dto.response.LoginLogResponse;
import com.synpharm.dto.response.UserResponse;
import com.synpharm.model.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 *
 * <p>定义用户信息管理相关的业务操作，包括查询、更新、密码管理等。
 * 注意：登录/注册职责已归 AuthService，本接口不再包含 login/register 方法。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
public interface UserService {

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    SysUser getUserById(Long userId);

    /**
     * 根据邮箱查询用户
     *
     * @param email 用户邮箱
     * @return 用户实体
     */
    SysUser getUserByEmail(String email);

    /**
     * 更新用户信息
     *
     * @param userId    用户ID
     * @param nickname  昵称（可为null表示不更新）
     * @param avatarUrl 头像URL（可为null表示不更新）
     * @return 更新后的用户响应
     */
    UserResponse updateUser(Long userId, String nickname, String avatarUrl);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 用户邮箱
     * @return 是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 获取当前用户信息
     *
     * @param token JWT令牌
     * @return 用户响应
     */
    UserResponse getProfile(String token);

    /**
     * 更新当前用户信息
     *
     * @param token   JWT令牌
     * @param request 用户信息更新请求
     * @return 更新后的用户响应
     */
    UserResponse updateProfile(String token, UserResponse request);

    /**
     * 修改密码
     *
     * @param token       JWT令牌
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String token, String oldPassword, String newPassword);

    /**
     * 删除账号
     *
     * @param token    JWT令牌
     * @param password 用户密码
     */
    void deleteAccount(String token, String password);

    /**
     * 绑定邮箱（账号当前未绑定邮箱时使用）
     *
     * <p>验证码发往待绑定的新邮箱，校验通过后置 email_verified=1。
     *
     * @param token JWT令牌
     * @param email 待绑定的邮箱
     * @param code  发往该邮箱的验证码
     * @return 更新后的用户响应
     */
    UserResponse bindEmail(String token, String email, String code);

    /**
     * 换绑邮箱
     *
     * <p>需同时校验当前密码与新邮箱验证码，防止账号被盗后直接换绑。
     *
     * @param token           JWT令牌
     * @param newEmail        新邮箱
     * @param code            发往新邮箱的验证码
     * @param currentPassword 当前密码
     * @return 更新后的用户响应
     */
    UserResponse changeEmail(String token, String newEmail, String code, String currentPassword);

    /**
     * 查询当前用户的登录记录
     *
     * @param token JWT令牌
     * @param limit 返回条数上限
     * @return 登录记录列表（按时间倒序）
     */
    List<LoginLogResponse> getLoginLogs(String token, int limit);
}
