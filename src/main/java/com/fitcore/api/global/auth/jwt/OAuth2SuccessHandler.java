package com.fitcore.api.global.auth.jwt;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

        // 1. 세션에서 모드 확인 및 데이터 추출
        HttpSession session = request.getSession(false);
        String oauthMode = (session != null) ? (String) session.getAttribute("oauth_mode") : null;

        // 2. 세션 데이터 정리 (성공했으므로 삭제)
        if (session != null) {
            session.removeAttribute("oauth_mode");
            session.removeAttribute("link_user_id");
        }

        // 3. Principal 체크
        if (authentication.getPrincipal() == null) {
            throw new IOException("인증 정보를 찾을 수 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof PrincipalDetails principalDetails) {
            if (principalDetails.getUser() != null) {
                String profileImageUrl = principalDetails.getUser().getProfileImageUrl();

                // 토큰 생성
                String token = tokenProvider.createToken(authentication, profileImageUrl);

                // 4. [중요] 리다이렉트 URL 생성 (토큰 + 모드 전달)
                UriComponentsBuilder builder =
                    UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                        .queryParam("token", token);

                // mode가 존재하면 파라미터로 추가 (프론트엔드에서 신규/링크 구분 가능)
                if (oauthMode != null) {
                    builder.queryParam("mode", oauthMode);
                }

                getRedirectStrategy().sendRedirect(request, response, builder.build().toUriString());
            } else {
                throw new IOException("유저 정보를 찾을 수 없습니다.");
            }
        } else {
            throw new IOException("인증 객체가 PrincipalDetails 타입이 아닙니다.");
        }
    }
}
