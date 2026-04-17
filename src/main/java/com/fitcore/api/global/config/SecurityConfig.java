package com.fitcore.api.global.config;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fitcore.api.global.auth.jwt.JwtFilter;
import com.fitcore.api.global.auth.jwt.OAuth2SuccessHandler;
import com.fitcore.api.global.auth.jwt.TokenProvider;
import com.fitcore.api.infrastructure.oauth.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // REST API이므로 CSRF 끔
            .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 끔
            .httpBasic(AbstractHttpConfigurer::disable) // Bearer 방식을 쓸 거라 기본 인증 끔

            // 세션을 사용하지 않음 (JWT 방식의 핵심)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 경로별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/set-link-mode").authenticated()
                .requestMatchers("/",
                    "/auth/**",
                    "/oauth2/**",
                    "/login/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )

            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )

            // JWT 필터 추가
            .addFilterBefore(new JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 도메인 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Set-Cookie"));
        // 자격 증명(쿠키, 인증 헤더) 허용
        configuration.setAllowCredentials(true);
        // 캐시 시간 설정 (초 단위)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
