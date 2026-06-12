package com.fitcore.api.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.domain.routine.service.RoutineService;
import com.fitcore.api.domain.routine.response.RoutineDraftResponse;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "CORS_ALLOWED_ORIGINS=https://fit-core-demo.trycloudflare.com",
    "app.auth.demo-token-enabled=true"
})
class SecurityConfigTest {

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
    void corsPreflightAllowsConfiguredExactOriginForRoutineGenerate() throws Exception {
        mockMvc.perform(options("/api/routines/generate")
                .header(HttpHeaders.ORIGIN, "https://fit-core-demo.trycloudflare.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://fit-core-demo.trycloudflare.com"));
    }

    @Test
    void demoBearerTokenCanReachRoutineGenerateInProductionLikeRequest() throws Exception {
        when(routineService.generateRoutine(any())).thenReturn(RoutineDraftResponse.builder()
            .routineDraftId("draft-demo")
            .generationStatus(GenerationStatus.success)
            .statusReasonCode(StatusReasonCode.none)
            .isFallback(false)
            .totalEstimatedTime(45)
            .summaryTitle("Demo routine")
            .rationaleSummary(List.of("demo"))
            .warnings(List.of())
            .routineBlocks(List.of())
            .build());

        mockMvc.perform(post("/api/routines/generate")
                .header(HttpHeaders.ORIGIN, "https://fit-core-demo.trycloudflare.com")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + demoToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetSplitLabel": "upper",
                      "targetMuscles": ["chest"],
                      "readinessLevel": "normal",
                      "timeAvailableMin": 45,
                      "currentPainAreas": [],
                      "currentDoms": [],
                      "unavailableEquipment": [],
                      "goal": "hypertrophy",
                      "userNote": "demo"
                    }
                    """.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routineDraftId").value("draft-demo"));
    }
}
