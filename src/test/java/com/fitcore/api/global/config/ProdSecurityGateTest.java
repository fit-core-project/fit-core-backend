package com.fitcore.api.global.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.domain.routine.service.RoutineService;
import com.fitcore.api.domain.routine.response.RoutineDraftResponse;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:prodgate;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false",
    "jwt.secret=prod-gate-test-secret-for-hs512-that-is-long-enough-2026-padding-padding",
    "CORS_ALLOWED_ORIGINS=https://example.vercel.app",
    "ai.server.url=http://127.0.0.1:9999/api/ai",
    "ai.enable-fallback=true",
    "app.auth.demo-token-enabled=false"
})
class ProdSecurityGateTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoutineService routineService;

    private static String demoToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString(mapper.writeValueAsBytes(Map.of("alg", "none", "typ", "JWT")));
        String payload = encoder.encodeToString(
            mapper.writeValueAsBytes(Map.of(
                "sub", "interviewer.demo@fit-core.local",
                "auth", "ROLE_ADMIN",
                "profileImage", "",
                "iat", 1,
                "exp", 4_102_444_800L,
                "demo", true
            ))
        );
        return header + "." + payload + ".demo";
    }

    @Test
    void demoBearerTokenIsRejectedInProdProfile() throws Exception {
        mockMvc.perform(post("/api/routines/generate")
                .header(HttpHeaders.ORIGIN, "https://example.vercel.app")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + demoToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetSplitLabel": "FULL_BODY",
                      "targetMuscles": ["chest"],
                      "readinessLevel": "NORMAL",
                      "timeAvailableMin": 30,
                      "currentPainAreas": [],
                      "currentDoms": [],
                      "unavailableEquipment": [],
                      "goal": "hypertrophy"
                    }
                    """.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void forgedDemoAdminTokenCannotReachAdminEndpointInProdProfile() throws Exception {
        mockMvc.perform(get("/api/admin/exercises")
                .header(HttpHeaders.ORIGIN, "https://example.vercel.app")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + demoToken()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointsArePermitAllInProdProfile() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.backend").value("up"));

        mockMvc.perform(get("/api/ai/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.backend").value("up"))
            .andExpect(jsonPath("$.aiBaseUrlConfigured").value(true))
            .andExpect(jsonPath("$.aiBaseUrlMode").value("root_plus_api_ai"))
            .andExpect(jsonPath("$.aiBaseUrlSanitized").value("http://127.0.0.1:9999"))
            .andExpect(jsonPath("$.aiEffectiveBasePath").value("/api/ai"))
            .andExpect(jsonPath("$.fallbackEnabled").value(true))
            .andExpect(jsonPath("$.fallbackConfigured").value(true))
            .andExpect(jsonPath("$.fallbackEffective").value(true))
            .andExpect(jsonPath("$.fallbackPolicy").value("always_on_for_safety"));
    }

    @Test
    void optionsRoutineGenerateIsNotForbiddenInProdProfile() throws Exception {
        mockMvc.perform(options("/api/routines/generate")
                .header(HttpHeaders.ORIGIN, "https://example.vercel.app")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://example.vercel.app"));
    }

    @Test
    void authenticatedRoutineGenerateCanReturnFallbackInProdProfile() throws Exception {
        when(routineService.generateRoutine(any())).thenReturn(RoutineDraftResponse.builder()
            .routineDraftId("draft-prod-gate")
            .generationStatus(GenerationStatus.fallback)
            .statusReasonCode(StatusReasonCode.ai_connection_refused)
            .isFallback(true)
            .totalEstimatedTime(30)
            .summaryTitle("기본 운동 루틴")
            .rationaleSummary(List.of("AI 서버가 현재 닫혀 있어 기본 루틴으로 대체했습니다."))
            .warnings(List.of("AI 서버 연결 실패로 개인화 정확도가 제한됩니다."))
            .routineBlocks(List.of())
            .build());

        mockMvc.perform(post("/api/routines/generate")
                .with(user("prod-user").roles("USER"))
                .header(HttpHeaders.ORIGIN, "https://example.vercel.app")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetSplitLabel": "FULL_BODY",
                      "targetMuscles": ["chest", "upper-back", "quadriceps"],
                      "readinessLevel": "NORMAL",
                      "timeAvailableMin": 30,
                      "currentPainAreas": [],
                      "currentDoms": [],
                      "unavailableEquipment": [],
                      "goal": "hypertrophy",
                      "userNote": "prod gate fallback test"
                    }
                    """.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.generationStatus").value("fallback"))
            .andExpect(jsonPath("$.statusReasonCode").value("ai_connection_refused"))
            .andExpect(jsonPath("$.isFallback").value(true));
    }
}
