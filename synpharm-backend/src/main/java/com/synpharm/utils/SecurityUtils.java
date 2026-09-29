package com.synpharm.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 安全上下文工具类
 *
 * <p>统一提供"当前登录用户ID"与"当前请求客户端IP"的获取方式：
 * <ul>
 *   <li>userId 取自 JwtAuthenticationFilter 写入 SecurityContext 的 principal（可信，无需再解析 token）</li>
 *   <li>IP 优先从当前请求上下文获取（限流等无 HttpServletRequest 参数的场景）</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 获取当前登录用户ID
     *
     * <p>JwtAuthenticationFilter 在认证通过后会把 Long userId 作为 principal
     * 写入 SecurityContext；未认证时返回 null（由调用方决定 401 处理）。
     *
     * @return 当前用户ID，未认证返回 null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        return null;
    }

    /**
     * 获取当前请求的客户端IP（基于 RequestContextHolder）
     *
     * <p>适用于 Service 层没有 HttpServletRequest 参数的场景（如验证码限流）。
     *
     * @return 客户端IP，获取不到返回 "unknown"
     */
    public static String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = IpUtils.getClientIp(request);
        return ip != null && !ip.isBlank() ? ip : "unknown";
    }
}
