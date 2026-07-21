package com.synpharm.config;

import com.synpharm.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 *
 * <p>拦截所有HTTP请求，验证JWT令牌并将用户信息存入SecurityContext。
 * 继承 OncePerRequestFilter 确保每个请求只过滤一次。
 *
 * <p>执行时机：在 UsernamePasswordAuthenticationFilter 之前，
 * Controller处理请求之前。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT工具类，用于解析和验证Token */
    private final JwtUtils jwtUtils;

    /** Redis操作（检查Token黑名单） */
    private final StringRedisTemplate redisTemplate;

    /** Token黑名单Key前缀 */
    private static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    /**
     * 执行认证过滤
     *
     * <p>流程：
     * <ol>
     *   <li>从请求头提取Token</li>
     *   <li>检查Token是否在黑名单</li>
     *   <li>验证Token有效性</li>
     *   <li>解析用户信息</li>
     *   <li>设置SecurityContext认证信息</li>
     *   <li>继续过滤器链</li>
     * </ol>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // ========== 第一步：提取Token ==========
            String token = extractToken(request);

            if (StringUtils.hasText(token)) {
                // ========== 第二步：验证Token有效性（一次解析，后面不再重复解析） ==========
                if (jwtUtils.validateToken(token)) {
                    // ========== 第三步：用 jti 检查黑名单（比用完整Token省内存） ==========
                    String jti = jwtUtils.getJtiFromToken(token);
                    if (jti != null && Boolean.TRUE.equals(
                            redisTemplate.hasKey(TOKEN_BLACKLIST_KEY + jti))) {
                        log.warn("Token已在黑名单中, jti: {}", jti);
                        // 黑名单中的Token等同于未登录，直接放行（后面会被Spring Security拦截）
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // ========== 第四步：解析用户信息 ==========
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token);

                    log.debug("Token验证通过, userId: {}, role: {}", userId, role);

                    // ========== 第五步：设置认证上下文 ==========
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId,  // principal：用户ID
                                    null,    // credentials：密码（JWT不需要）
                                    // 权限列表：Spring Security要求 ROLE_ 前缀
                                    Collections.singletonList(
                                            new SimpleGrantedAuthority("ROLE_" + role)
                                    )
                            );

                    // 设置请求详情（IP、SessionID等，用于审计）
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 将认证信息存入SecurityContext（基于ThreadLocal，当前线程内有效）
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token验证过程中出现任何异常，都清除认证上下文
            log.error("JWT认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // ========== 第六步：继续过滤器链 ==========
        filterChain.doFilter(request, response);
    }

    /**
     * 从Authorization头中提取Token
     * <p>格式：Authorization: Bearer <token>
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 截取Bearer后面的部分（从索引7开始）
            return bearerToken.substring(7);
        }
        return null;
    }
}
