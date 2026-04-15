package com.fitcore.api.global.auth.jwt;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fitcore.api.global.auth.PrincipalDetails;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        // 1. Principal 체크 (null 방지)
        if (authentication.getPrincipal() == null) {
            throw new IOException("인증 정보를 찾을 수 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        // 2. 타입 안전성 체크 (instanceof 사용)
        if (principal instanceof PrincipalDetails principalDetails) {
            // 3. User 엔티티와 프로필 이미지 존재 여부 확인
            if (principalDetails.getUser() != null) {
                String profileImageUrl = principalDetails.getUser().getProfileImageUrl();

                // 토큰 생성
                String token = tokenProvider.createToken(authentication, profileImageUrl);

                // 리다이렉트
                String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                    .queryParam("token", token)
                    .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } else {
                throw new IOException("유저 정보를 찾을 수 없습니다.");
            }
        } else {
            throw new IOException("인증 객체가 PrincipalDetails 타입이 아닙니다.");
        }
    }
}
