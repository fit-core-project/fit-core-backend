package com.fitcore.api.global.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
    @Mock
    private TokenProvider tokenProvider;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void demoTokenIsRejectedWhenDisabled() throws Exception {
        JwtFilter filter = new JwtFilter(tokenProvider, false);
        MockHttpServletRequest request = requestWithBearer(demoToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void demoTokenAuthenticatesWhenEnabled() throws Exception {
        JwtFilter filter = new JwtFilter(tokenProvider, true);
        MockHttpServletRequest request = requestWithBearer(demoToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
            .extracting("authority")
            .contains("ROLE_ADMIN", "ROLE_USER");
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void normalJwtAuthenticationStillUsesTokenProviderWhenDemoTokenDisabled() throws Exception {
        String jwt = "normal.jwt.token";
        var authentication = new UsernamePasswordAuthenticationToken(
            "user-001",
            jwt,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(tokenProvider.validateToken(jwt)).thenReturn(true);
        when(tokenProvider.getAuthentication(jwt)).thenReturn(authentication);

        JwtFilter filter = new JwtFilter(tokenProvider, false);
        MockHttpServletRequest request = requestWithBearer(jwt);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
        verify(tokenProvider).validateToken(jwt);
        verify(tokenProvider).getAuthentication(jwt);
    }

    private MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        return request;
    }

    private static String demoToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString(mapper.writeValueAsBytes(Map.of("alg", "none", "typ", "JWT")));
        String payload = encoder.encodeToString(
            mapper.writeValueAsString(Map.of(
                "sub", "interviewer.demo@fit-core.local",
                "auth", "ROLE_ADMIN",
                "profileImage", "",
                "iat", 1,
                "exp", 4_102_444_800L,
                "demo", true
            )).getBytes(StandardCharsets.UTF_8)
        );
        return header + "." + payload + ".demo";
    }
}
