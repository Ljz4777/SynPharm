package com.synpharm.service;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 *
 * <p>作为登录的总入口，负责登录限流检查、委托给具体登录策略、
 * 登录日志记录、登出处理等。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
public interface AuthService {

    /**
     * 登录总入口
     * <p>支持多种登录方式，通过 loginType 区分，新用户自动注册。
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求对象
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 用户登出
     * <p>将Token加入黑名单，使其立即失效。
     *
     * @param token JWT令牌
     */
    void logout(String token);
}
