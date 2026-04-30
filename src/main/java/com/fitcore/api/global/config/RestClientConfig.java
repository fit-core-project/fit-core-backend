package com.fitcore.api.global.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Bean
    public RestClient aiRestClient() {
        return RestClient.builder()
            .baseUrl(aiServerUrl)
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .requestInterceptor((request, body, execution) -> {
                // 💡 나가는 요청 내용을 콘솔에 출력
                System.out.println(">>> [DEBUG] Request Method: " + request.getMethod());
                System.out.println(">>> [DEBUG] Request Headers: " + request.getHeaders());
                System.out.println(">>> [DEBUG] Request Body: " + new String(body));
                return execution.execute(request, body);
            })
            .build();
    }
}
