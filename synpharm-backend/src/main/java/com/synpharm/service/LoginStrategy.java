package com.synpharm.service;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录策略接口
 *
 * <p>所有登录方式都必须实现这个接口。这是策略模式的核心，
 * 定义了登录方式的"契约"。
 *
 * <p>设计原则：
 * <ul>
 *   <li>单一职责：每个实现类只负责一种登录方式</li>
 *   <li>开闭原则：新增登录方式只需新增实现类，不改现有代码</li>
 *   <li>依赖倒置：上层依赖此接口，不依赖具体实现</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
public interface LoginStrategy {

    /**
     * 获取该策略支持的登录类型
     * <p>用于策略工厂初始化时构建映射关系。
     *
     * @return 登录类型标识，如 "qq_email"、"phone"、"wechat"
     */
    String getLoginType();

    /**
     * 执行登录逻辑
     * <p>具体的登录校验、用户查询/创建等逻辑都在这里实现。
     *
     * @param request     登录请求参数
     * @param httpRequest HTTP请求对象（用于获取IP、User-Agent等）
     * @return 登录响应（包含Token和用户信息）
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
}
