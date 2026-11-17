package com.fitcore.api.global.auth.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

public class JwtFilter extends GenericFilterBean {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider tokenProvider;
    private final boolean demoTokenEnabled;
    /**
     * The demo token is unsigned and its shape ships in the public frontend bundle, so anyone can mint one.
     * It is ROLE_USER only unless this is explicitly turned on (local/dev admin demos); never enable it in prod.
     */
    private final boolean demoTokenAdminEnabled;

    public JwtFilter(TokenProvider tokenProvider, boolean demoTokenEnabled) {
        this(tokenProvider, demoTokenEnabled, false);
    }

    public JwtFilter(TokenProvider tokenProvider, boolean demoTokenEnabled, boolean demoTokenAdminEnabled) {
        this.tokenProvider = tokenProvider;
        this.demoTokenEnabled = demoTokenEnabled;
        this.demoTokenAdminEnabled = demoTokenAdminEnabled;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String jwt = resolveToken(httpServletRequest);

        if (StringUtils.hasText(jwt) && isDemoToken(jwt)) {
            if (!demoTokenEnabled) {
                SecurityContextHolder.clearContext();
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Demo token authentication is disabled");
                return;
            }
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                "demo-user-001",
                jwt,
                demoTokenAdminEnabled
                    ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                    : List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            Authentication authentication;
            try {
                authentication = tokenProvider.getAuthentication(jwt);
            } catch (AuthenticationException e) {
                // 서명·만료는 유효하지만 사용자가 탈퇴(soft delete)했거나 다른 계정이 된 토큰.
                // 익명으로 흘려보내면 엔트리포인트에 따라 302/403이 섞이므로 여기서 401로 끊는다.
                // sendError는 /error 재디스패치에서 다시 인증 검사를 타므로 본문을 직접 쓴다.
                SecurityContextHolder.clearContext();
                writeInactiveUserResponse(httpServletResponse);
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private void writeInactiveUserResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
            "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"code\":\"U003\","
                + "\"message\":\"탈퇴했거나 더 이상 유효하지 않은 계정입니다. 다시 로그인해 주세요.\"}"
        );
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isDemoToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2 || !"demo".equals(parts[parts.length - 1])) {
            return false;
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return payload.contains("\"demo\":true") && payload.contains("interviewer.demo@fit-core.local");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
