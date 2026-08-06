package com.synpharm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${fastapi.base-url}")
    private String baseUrl;

    @Value("${fastapi.timeout-single}")
    private int timeoutSingle;

    @Value("${fastapi.timeout-batch}")
    private int timeoutBatch;

    @Value("${fastapi.api-key:}")
    private String apiKey;

    @Bean
    public WebClient fastApiWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // 配置了 API Key 时，调用算法引擎自动携带 X-API-Key 请求头
        if (StringUtils.hasText(apiKey)) {
            builder.defaultHeader("X-API-Key", apiKey);
        }

        return builder.build();
    }

    @Bean("singleTimeout")
    public Duration singleTimeout() {
        return Duration.ofMillis(timeoutSingle);
    }

    @Bean("batchTimeout")
    public Duration batchTimeout() {
        return Duration.ofMillis(timeoutBatch);
    }
}
