package com.fitcore.api.global.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    // CORS는 SecurityConfig의 CorsConfigurationSource 빈에서 단일 관리 (CORS_ALLOWED_ORIGINS env)
}
