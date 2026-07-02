package com.fitcore.api.global.config;

import java.util.Arrays;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class RestClientConfig {

    // AI routine generation can take longer than ordinary API calls during demos.
    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${ai.client.connect-timeout-seconds:3}")
    private int connectTimeoutSeconds;

    @Value("${ai.client.response-timeout-seconds:180}")
    private int responseTimeoutSeconds;

    private final Environment environment;

    public RestClientConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public RestClient aiRestClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(connectTimeoutSeconds))
            .setConnectionRequestTimeout(Timeout.ofSeconds(connectTimeoutSeconds))
            .setResponseTimeout(Timeout.ofSeconds(responseTimeoutSeconds))
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
                log.debug("AI request method={} headers={} bodyBytes={}", request.getMethod(), request.getHeaders(), body.length);
                return execution.execute(request, body);
            });
        }

        return builder.build();
    }
}
