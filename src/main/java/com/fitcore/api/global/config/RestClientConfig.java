package com.fitcore.api.global.config;

import java.util.Arrays;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // AI 응답 대기 시간 — Gemini 생성이 느릴 수 있어 넉넉히 설정.
    // 튜닝 필요 시 이 두 상수만 조정.
    private static final int CONNECT_TIMEOUT_SECONDS = 3;
    private static final int RESPONSE_TIMEOUT_SECONDS = 60;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private final Environment environment;

    public RestClientConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public RestClient aiRestClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .setConnectionRequestTimeout(Timeout.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .setResponseTimeout(Timeout.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
            .build();

        var httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();

        RestClient.Builder builder = RestClient.builder()
            .baseUrl(aiServerUrl)
            .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // prod 프로파일에서는 request body 미출력 (user note/pain/equipment 등 민감 데이터 보호)
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!isProd) {
            builder.requestInterceptor((request, body, execution) -> {
                System.out.println(">>> [DEBUG] Request Method: " + request.getMethod());
                System.out.println(">>> [DEBUG] Request Headers: " + request.getHeaders());
                System.out.println(">>> [DEBUG] Request Body: " + new String(body));
                return execution.execute(request, body);
            });
        }

        return builder.build();
    }
}
