package com.synpharm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 *
 * <p>配置Spring Boot应用的跨域资源共享(CORS)策略。
 * B-07 修复：来源从配置读取（app.cors.allowed-origins，逗号分隔），
 * 不再使用 "*" + allowCredentials（该组合本身就是浏览器禁止的无效配置）。
 * 生产环境经 nginx 同源反代，可将配置留空以完全关闭 CORS。
 *
 * <p>提供两个Bean：
 * <ul>
 *   <li>corsConfigurationSource: 供Spring Security的 .cors() 使用</li>
 *   <li>corsFilter: 供非Security场景使用</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 3.0.0
 */
@Configuration
public class CorsConfig {

    /** 允许的来源列表（逗号分隔），为空时关闭 CORS */
    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 配置CORS规则
     *
     * @return CorsConfiguration CORS配置
     */
    private CorsConfiguration buildCorsConfig() {
        CorsConfiguration config = new CorsConfiguration();

        // B-07：白名单来源（不再允许任意源）
        for (String origin : allowedOrigins.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                config.addAllowedOriginPattern(trimmed);
            }
        }

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有HTTP方法
        config.addAllowedMethod("*");

        // 允许携带凭证（如Cookie、Authorization头）
        config.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        return config;
    }

    /**
     * 配置CORS配置源
     * <p>供Spring Security的 .cors().configurationSource() 使用。
     *
     * @return CorsConfigurationSource CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildCorsConfig());
        return source;
    }

    /**
     * 配置跨域过滤器
     * <p>供非Security场景使用。
     *
     * @return CorsFilter 跨域过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}
