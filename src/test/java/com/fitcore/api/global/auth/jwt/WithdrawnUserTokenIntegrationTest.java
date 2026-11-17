package com.fitcore.api.global.auth.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.UserStatus;
import com.fitcore.api.domain.user.repository.UserRepository;
import com.fitcore.api.global.auth.PrincipalDetails;

/**
 * 탈퇴(soft delete) 후에도 만료 전 JWT가 남아 있을 때, 보호된 엔드포인트가 401로 거부하는지 확인한다.
 * 실제 보안 필터 체인(addFilters 기본값 true)과 H2 test 프로필을 쓴다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WithdrawnUserTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;

    @Test
    void withdrawnUsersStillValidTokenIsRejectedWith401OnProtectedWrite() throws Exception {
        String email = "withdraw-" + UUID.randomUUID() + "@fitcore.test";
        UserProfileEntity user = userRepository.saveAndFlush(UserProfileEntity.builder()
            .email(email)
            .nickname("bye-" + UUID.randomUUID().toString().substring(0, 8))
            .status(UserStatus.ACTIVE)
            .build());
        String token = issueToken(user);

        // 탈퇴 전에는 같은 토큰으로 인증된다.
        mockMvc.perform(get("/api/profile/me").header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token))
            .andExpect(status().isOk());

        // 실제 탈퇴 API로 탈퇴한다.
        mockMvc.perform(delete("/api/profile/me").header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token))
            .andExpect(status().is2xxSuccessful());

        // 서명·만료가 유효한 같은 토큰으로 운동 기록 쓰기를 시도하면 401.
        mockMvc.perform(post("/api/workouts")
                .header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("U003"));

        mockMvc.perform(get("/api/profile/me").header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenOfWithdrawnAccountDoesNotAuthenticateNewAccountWithSameEmail() throws Exception {
        String email = "rejoin-" + UUID.randomUUID() + "@fitcore.test";
        UserProfileEntity original = userRepository.saveAndFlush(UserProfileEntity.builder()
            .email(email)
            .status(UserStatus.ACTIVE)
            .build());
        String oldToken = issueToken(original);

        mockMvc.perform(delete("/api/profile/me").header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + oldToken))
            .andExpect(status().is2xxSuccessful());

        // 탈퇴 시 이메일이 익명화되므로 같은 소셜 이메일로 재가입할 수 있다.
        UserProfileEntity rejoined = userRepository.saveAndFlush(UserProfileEntity.builder()
            .email(email)
            .status(UserStatus.ACTIVE)
            .build());

        mockMvc.perform(get("/api/profile/me").header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + oldToken))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profile/me")
                .header(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + issueToken(rejoined)))
            .andExpect(status().isOk());
    }

    private String issueToken(UserProfileEntity user) {
        var authentication = new UsernamePasswordAuthenticationToken(
            new PrincipalDetails(user),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return tokenProvider.createToken(authentication, null);
    }
}
