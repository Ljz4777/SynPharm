package com.synpharm.service;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.request.RegisterRequest;
import com.synpharm.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 *
 * <p>作为登录的总入口，负责登录限流检查、委托给具体登录策略、
 * 登录日志记录、登出处理、用户注册等。
 *
 * @author SynPharm Team
 * @version 2.1.0
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
     * 用户注册
     * <p>使用QQ邮箱验证码进行注册，验证码需要提前通过发送验证码接口获取。
     *
     * @param request     注册请求
     * @param httpRequest HTTP请求对象
     * @return 注册成功后自动登录的响应
     */
    LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * 用户登出
     * <p>将Token加入黑名单，使其立即失效。
     *
     * @param token JWT令牌
     */
    void logout(String token);
}
