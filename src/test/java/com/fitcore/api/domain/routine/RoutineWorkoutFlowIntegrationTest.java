package com.fitcore.api.domain.routine;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.domain.routine.dto.Prescription;
import com.fitcore.api.domain.routine.dto.RoutineBlock;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.infrastructure.ai.client.AiClient;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineRequest;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RoutineWorkoutFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SecurityUtils securityUtils;

    @MockitoBean
    private AiClient aiClient;

    @Test
    void generateFinalizeWorkoutFlowPersistsThroughPublicContract() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("demo-user-001");
        when(aiClient.generateRoutine(any())).thenReturn(successAiResponse());

        String generateResponse = mockMvc.perform(post("/api/routines/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetSplitLabel":"push",
                      "targetMuscles":["chest","triceps"],
                      "readinessLevel":"normal",
                      "timeAvailableMin":60,
                      "currentPainAreas":[],
                      "currentDoms":[],
                      "unavailableEquipment":[],
                      "goal":"hypertrophy",
                      "userNote":"demo"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.generationStatus").value("success"))
            .andExpect(jsonPath("$.routineBlocks[0].prescription[0].setIndex").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();

        ArgumentCaptor<AiRoutineRequest> aiRequestCaptor = ArgumentCaptor.forClass(AiRoutineRequest.class);
        verify(aiClient).generateRoutine(aiRequestCaptor.capture());
        Assertions.assertThat(aiRequestCaptor.getValue().getTargetMuscles())
            .containsExactly("chest", "triceps");

        JsonNode generated = objectMapper.readTree(generateResponse);
        String draftId = generated.get("routineDraftId").asText();

        String finalizeResponse = mockMvc.perform(post("/api/routines/drafts/{routineDraftId}/finalize", draftId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetWorkoutDate":"2026-05-18",
                      "finalRoutinePayload":{
                        "generationStatus":"success",
                        "statusReasonCode":"none",
                        "fallback":false,
                        "totalEstimatedTime":45,
                        "summaryTitle":"demo routine",
                        "rationaleSummary":["demo"],
                        "warnings":[],
                        "routineBlocks":[{
                          "order":1,
                          "exerciseId":"barbell_bench_press",
                          "exerciseName":"Barbell Bench Press",
                          "primaryMuscles":["chest"],
                          "defaultRestSec":120,
                          "exerciseRationale":"demo",
                          "prescription":[{
                            "setIndex":1,
                            "setType":"working",
                            "targetReps":8,
                            "targetWeightKg":70,
                            "targetRir":2,
                            "targetRestSec":120
                          }]
                        }]
                      },
                      "acceptedWithoutEdits":true,
                      "userEditSummary":[]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routineDraftId").value(draftId))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String finalId = objectMapper.readTree(finalizeResponse).get("routineFinalId").asText();

        mockMvc.perform(post("/api/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "workoutDate":"2026-05-18",
                      "splitLabel":"push",
                      "sourceRoutineFinalId":"%s",
                      "timeAvailableMin":60,
                      "durationMin":45,
                      "readinessLevel":"normal",
                      "currentPainAreas":[],
                      "currentDoms":[],
                      "unavailableEquipment":[],
                      "sets":[{
                        "exerciseOrder":1,
                        "exerciseId":"barbell_bench_press",
                        "exerciseNameSnapshot":"Barbell Bench Press",
                        "setIndex":1,
                        "setType":"working",
                        "trackingMode":"weightReps",
                        "weightKg":70,
                        "reps":8,
                        "rir":2,
                        "isFailure":false,
                        "restSec":120
                      }]
                    }
                    """.formatted(finalId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sourceRoutineFinalId").value(finalId))
            .andExpect(jsonPath("$.sets[0].exerciseOrder").value(1));
    }

    @Test
    void workoutRejectsMissingSetsWithBadRequestInsteadOfServerError() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("demo-user-001");

        mockMvc.perform(post("/api/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "workoutDate":"2026-05-18",
                      "splitLabel":"push"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void aiFailureFallbackUsesSpreadsheetSlugs() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("demo-user-001");
        when(aiClient.generateRoutine(any())).thenThrow(new RuntimeException("timeout"));

        mockMvc.perform(post("/api/routines/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetSplitLabel":"push",
                      "targetMuscles":["chest","triceps"],
                      "readinessLevel":"normal",
                      "timeAvailableMin":60,
                      "currentPainAreas":[],
                      "currentDoms":[],
                      "unavailableEquipment":[],
                      "goal":"hypertrophy",
                      "userNote":"demo"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routineBlocks[0].primaryMuscles[0]").value("chest"))
            .andExpect(jsonPath("$.routineBlocks[0].primaryMuscles[1]").value("triceps"))
            .andExpect(jsonPath("$.routineBlocks[0].equipmentType").value("BODYWEIGHT"));
    }

    private AiRoutineResponse successAiResponse() {
        Prescription prescription = new Prescription();
        prescription.setSetIndex(1);
        prescription.setSetType("working");
        prescription.setTargetReps(8);
        prescription.setTargetWeightKg(BigDecimal.valueOf(70));
        prescription.setTargetRir(2);
        prescription.setTargetRestSec(120);

        RoutineBlock block = new RoutineBlock();
        block.setOrder(1);
        block.setExerciseId("barbell_bench_press");
        block.setExerciseName("Barbell Bench Press");
        block.setPrimaryMuscles(List.of("chest"));
        block.setDefaultRestSec(120);
        block.setPrescription(List.of(prescription));
        block.setExerciseRationale("demo");
        block.setSubstitutionCandidates(List.of());

        return AiRoutineResponse.builder()
            .generationStatus(GenerationStatus.success)
            .statusReasonCode(StatusReasonCode.none)
            .isFallback(false)
            .totalEstimatedTime(45)
            .summaryTitle("demo routine")
            .rationaleSummary(List.of("demo"))
            .routineBlocks(List.of(block))
            .warnings(List.of())
            .build();
    }
}

