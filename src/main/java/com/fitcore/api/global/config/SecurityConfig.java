package com.fitcore.api.global.config;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001}")
    private String corsAllowedOriginsRaw;

    @Value("${app.auth.demo-token-enabled:false}")
    private boolean demoTokenEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // REST API이므로 CSRF 끔
            .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 끔
            .httpBasic(AbstractHttpConfigurer::disable) // Bearer 방식을 쓸 거라 기본 인증 끔

            // OAuth2 state 파라미터 저장을 위해 IF_REQUIRED 사용 (OAuth2 flow에서만 세션 생성)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // 경로별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/dev/**").authenticated()  // prod에선 @Profile("!prod")로 미등록, local은 인증 필요
                .requestMatchers("/api/v1/auth/set-link-mode").authenticated()
                .requestMatchers("/",
                    "/api/health",
                    "/api/ai/health",
                    "/actuator/health",
                    "/auth/**",
                    "/oauth2/**",
                    "/login/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            );

        // OAuth2 provider env가 하나라도 설정된 경우에만 oauth2Login 활성화
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            );
        }

        http
            // JWT 필터 추가
            .addFilterBefore(new JwtFilter(tokenProvider, demoTokenEnabled), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(corsAllowedOriginsRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Set-Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
