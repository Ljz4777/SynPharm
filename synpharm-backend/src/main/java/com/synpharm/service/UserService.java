package com.synpharm.service;

import com.synpharm.dto.response.UserResponse;
import com.synpharm.model.entity.SysUser;

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
}
