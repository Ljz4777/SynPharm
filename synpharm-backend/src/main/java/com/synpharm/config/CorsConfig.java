package com.synpharm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 *
 * <p>配置Spring Boot应用的跨域资源共享(CORS)策略，
 * 允许前端应用跨域访问后端API。
 *
 * <p>提供两个Bean：
 * <ul>
 *   <li>corsConfigurationSource: 供Spring Security的 .cors() 使用</li>
 *   <li>corsFilter: 供非Security场景使用</li>
 * </ul>
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS规则
     *
     * @return CorsConfiguration CORS配置
     */
    private CorsConfiguration buildCorsConfig() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有源（生产环境应限制具体域名）
        config.addAllowedOriginPattern("*");

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
