package com.synpharm.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 *
 * <p>提供从HTTP请求中获取客户端真实IP地址的方法。
 * 考虑了反向代理（Nginx等）场景下的X-Forwarded-For和X-Real-IP头。
 *
 * @author SynPharm Team
 * @version 1.0.0
 */
public final class IpUtils {

    private IpUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 获取客户端真实IP地址
     *
     * <p>优先级：
     * <ol>
     *   <li>X-Forwarded-For（反向代理常用）</li>
     *   <li>X-Real-IP（Nginx配置）</li>
     *   <li>RemoteAddr（直连场景）</li>
     * </ol>
     *
     * <p>注意：X-Forwarded-For 可被客户端伪造，生产环境应配置可信代理。
     * 多个代理时，取第一个IP（最左侧）为真实客户端IP。
     *
     * @param request HTTP请求
     * @return 客户端IP地址，获取不到返回 null
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个IP是真实客户端IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
