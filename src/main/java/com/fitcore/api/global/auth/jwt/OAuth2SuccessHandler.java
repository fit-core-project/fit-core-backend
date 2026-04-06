package com.fitcore.api.global.auth.jwt;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        // 1. 토큰 생성
        String token = tokenProvider.createToken(authentication);

        // 2. 프론트엔드 주소로 리다이렉트 (예: localhost:3000)
        // 쿼리 파라미터로 토큰을 넘겨주면 프론트가 이를 저장합니다.
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
            .queryParam("token", token)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
