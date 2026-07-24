package com.synpharm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @Bean
    public WebClient fastApiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
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
