package com.synpharm.config;

import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
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
import java.util.concurrent.TimeUnit;

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

    /** 用户验人缓存Key前缀（值格式 "status:tokenVersion"，如 "1:0"） */
    private static final String USER_AUTH_KEY = "user:auth:";

    /** 用户验人缓存时长（秒）——减少每个请求的查库开销 */
    private static final long USER_AUTH_CACHE_SECONDS = 60;

    /** 用户Mapper（验人：存在性 + 状态 + token 版本） */
    private final SysUserMapper sysUserMapper;

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

                    // ========== 第四步：验人（B-04）——校验用户仍存在、未禁用、token 版本一致 ==========
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    if (!isUserStillValid(userId, jwtUtils.getTokenVersionFromToken(token))) {
                        // 不设置认证上下文，放行到链尾由 Spring Security 入口点返回 401
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // ========== 第五步：解析用户信息 ==========
                    String role = jwtUtils.getRoleFromToken(token);

                    log.debug("Token验证通过, userId: {}, role: {}", userId, role);

                    // ========== 第六步：设置认证上下文 ==========
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
            log.error("JWT认证失败", e);
            SecurityContextHolder.clearContext();
        }

        // ========== 第七步：继续过滤器链 ==========
        filterChain.doFilter(request, response);
    }

    /**
     * 验人检查（B-04）：用户存在、状态启用且 token 版本与数据库一致才有效。
     *
     * <p>结果以 "status:tokenVersion" 形式缓存 60 秒，避免每个请求都查库；
     * Redis/DB 异常时 fail-closed（拒绝认证），防止绕过。
     *
     * @param userId   token 中的用户ID
     * @param tokenVer token 中的版本号
     * @return true=有效，false=无效（用户不存在/已禁用/版本不符/依赖异常）
     */
    private boolean isUserStillValid(Long userId, Integer tokenVer) {
        if (userId == null) {
            return false;
        }
        String cacheKey = USER_AUTH_KEY + userId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            boolean enabled;
            int currentVer;
            if (cached != null) {
                String[] parts = cached.split(":", 2);
                enabled = "1".equals(parts[0]);
                currentVer = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            } else {
                SysUser user = sysUserMapper.selectById(userId);
                if (user == null) {
                    // 用户不存在同样缓存，避免反复查库
                    redisTemplate.opsForValue().set(cacheKey, "0:0",
                            USER_AUTH_CACHE_SECONDS, TimeUnit.SECONDS);
                    return false;
                }
                enabled = Integer.valueOf(1).equals(user.getStatus());
                currentVer = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
                redisTemplate.opsForValue().set(cacheKey, (enabled ? "1" : "0") + ":" + currentVer,
                        USER_AUTH_CACHE_SECONDS, TimeUnit.SECONDS);
            }
            return enabled && tokenVer != null && tokenVer == currentVer;
        } catch (Exception e) {
            // 验人依赖不可用（Redis/DB 异常）：fail-closed，本次请求按未认证处理
            log.error("验人检查失败, userId: {}", userId, e);
            return false;
        }
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
