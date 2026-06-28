package com.fitcore.api.domain.routine;

import com.fitcore.api.domain.routine.dto.FinalRoutinePayload;
import com.fitcore.api.domain.routine.dto.Prescription;
import com.fitcore.api.domain.routine.dto.RoutineBlock;
import com.fitcore.api.domain.routine.response.RoutineDraftResponse;
import com.fitcore.api.domain.routine.response.RoutineFinalResponse;
import com.fitcore.api.domain.routine.service.RoutineService;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RoutineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoutineService routineService;

    private Prescription stubPrescription(int setIndex) {
        Prescription p = new Prescription();
        p.setSetIndex(setIndex);
        p.setSetType("working");
        p.setTargetReps(8);
        p.setTargetWeightKg(BigDecimal.valueOf(80));
        p.setTargetRir(2);
        p.setTargetRestSec(120);
        return p;
    }

    private RoutineBlock stubBlock(int order, String exerciseId, String exerciseName) {
        RoutineBlock block = new RoutineBlock();
        block.setOrder(order);
        block.setExerciseId(exerciseId);
        block.setExerciseName(exerciseName);
        block.setMovementPattern("horizontalPush");
        block.setPrimaryMuscles(List.of("chest"));
        block.setEquipmentType("BARBELL");
        block.setDefaultRestSec(120);
        block.setPrescription(List.of(stubPrescription(1), stubPrescription(2)));
        block.setExerciseRationale("기초 복합 운동");
        block.setSubstitutionCandidates(List.of());
        return block;
    }

    private FinalRoutinePayload stubPayload(List<RoutineBlock> blocks) {
        FinalRoutinePayload payload = new FinalRoutinePayload();
        payload.setFallback(false);
        payload.setGenerationStatus(GenerationStatus.success);
        payload.setStatusReasonCode(StatusReasonCode.none);
        payload.setSummaryTitle("푸시 루틴");
        payload.setTotalEstimatedTime(60);
        payload.setRationaleSummary(List.of("가슴/삼두 위주 구성"));
        payload.setWarnings(List.of());
        payload.setRoutineBlocks(blocks);
        return payload;
    }

    private RoutineFinalResponse stubFinal(String id, String splitLabel, LocalDate date) {
        return RoutineFinalResponse.builder()
            .routineFinalId(id)
            .routineDraftId("draft-" + id)
            .userId("user-001")
            .targetSplitLabel(splitLabel)
            .targetWorkoutDate(date)
            .finalRoutinePayload(stubPayload(List.of(
                stubBlock(1, "barbell_bench_press", "Barbell Bench Press"),
                stubBlock(2, "cable_tricep_pushdown", "Cable Tricep Pushdown")
            )))
            .acceptedWithoutEdits(true)
            .userEditSummary(List.of())
            .savedAt(LocalDateTime.of(2026, 5, 20, 10, 0))
            .build();
    }

    private RoutineDraftResponse stubGeneratedDraft() {
        return RoutineDraftResponse.builder()
            .routineDraftId("draft-validation")
            .generationStatus(GenerationStatus.success)
            .statusReasonCode(StatusReasonCode.none)
            .isFallback(false)
            .totalEstimatedTime(70)
            .summaryTitle("Validation routine")
            .rationaleSummary(List.of("valid request"))
            .warnings(List.of())
            .routineBlocks(List.of(stubBlock(1, "barbell_bench_press", "Barbell Bench Press")))
            .build();
    }

    private String validGenerateRequestJson() {
        return """
            {
              "targetSplitLabel": "push",
              "targetMuscles": ["chest", "front-deltoids", "triceps"],
              "readinessLevel": "normal",
              "timeAvailableMin": 70,
              "currentPainAreas": ["front-deltoids"],
              "currentDoms": [
                {"bodyPart": "chest", "level": "moderate"},
                {"bodyPart": "upper-back", "level": "mild"}
              ],
              "unavailableEquipment": ["MACHINE"],
              "goal": "hypertrophy",
              "userNote": "demo request"
            }
            """;
    }

    private void expectGenerateBadRequest(String json) throws Exception {
        mockMvc.perform(post("/api/routines/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void generate_validGoldenFixtureShape_returnsOk() throws Exception {
        when(routineService.generateRoutine(any())).thenReturn(stubGeneratedDraft());

        mockMvc.perform(post("/api/routines/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validGenerateRequestJson().getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routineDraftId").value("draft-validation"));
    }

    @Test
    void generate_invalidTimeAvailableMin_returnsBadRequest() throws Exception {
        expectGenerateBadRequest(validGenerateRequestJson().replace("\"timeAvailableMin\": 70", "\"timeAvailableMin\": 5"));
        verifyNoInteractions(routineService);
    }

    @Test
    void generate_blankGoal_returnsBadRequest() throws Exception {
        expectGenerateBadRequest(validGenerateRequestJson().replace("\"goal\": \"hypertrophy\"", "\"goal\": \" \""));
        verifyNoInteractions(routineService);
    }

    @Test
    void generate_emptyTargetMuscles_returnsBadRequest() throws Exception {
        expectGenerateBadRequest(validGenerateRequestJson()
            .replace("\"targetMuscles\": [\"chest\", \"front-deltoids\", \"triceps\"]", "\"targetMuscles\": []"));
        verifyNoInteractions(routineService);
    }

    @Test
    void generate_blankListItem_returnsBadRequest() throws Exception {
        expectGenerateBadRequest(validGenerateRequestJson()
            .replace("\"currentPainAreas\": [\"front-deltoids\"]", "\"currentPainAreas\": [\" \"]"));
        verifyNoInteractions(routineService);
    }

    @Test
    void generate_invalidReadinessLevel_returnsBadRequest() throws Exception {
        expectGenerateBadRequest(validGenerateRequestJson().replace("\"readinessLevel\": \"normal\"", "\"readinessLevel\": \"extreme\""));
        verifyNoInteractions(routineService);
    }

    // ── GET /api/routines/finals ────────────────────────────────────

    @Test
    void getMyFinalRoutines_returnsPaginatedList() throws Exception {
        when(routineService.getMyFinalRoutines(any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(
                stubFinal("final-001", "push", LocalDate.of(2026, 5, 20)),
                stubFinal("final-002", "pull", LocalDate.of(2026, 5, 18))
            ))
        );

        mockMvc.perform(get("/api/routines/finals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].routineFinalId").value("final-001"))
            .andExpect(jsonPath("$.content[0].targetSplitLabel").value("push"))
            .andExpect(jsonPath("$.content[0].targetWorkoutDate").value("2026-05-20"))
            .andExpect(jsonPath("$.content[0].finalRoutinePayload.summaryTitle").value("푸시 루틴"))
            .andExpect(jsonPath("$.content[0].finalRoutinePayload.routineBlocks.length()").value(2))
            .andExpect(jsonPath("$.content[0].finalRoutinePayload.routineBlocks[0].exerciseName")
                .value("Barbell Bench Press"))
            .andExpect(jsonPath("$.content[1].routineFinalId").value("final-002"))
            .andExpect(jsonPath("$.content[1].targetSplitLabel").value("pull"));
    }

    @Test
    void getMyFinalRoutines_emptyList_returnsEmptyPage() throws Exception {
        when(routineService.getMyFinalRoutines(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/routines/finals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getMyFinalRoutines_routineBlocksHavePrescriptions() throws Exception {
        when(routineService.getMyFinalRoutines(any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(stubFinal("final-001", "push", LocalDate.of(2026, 5, 20))))
        );

        mockMvc.perform(get("/api/routines/finals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].finalRoutinePayload.routineBlocks[0].prescription.length()").value(2))
            .andExpect(jsonPath("$.content[0].finalRoutinePayload.routineBlocks[0].prescription[0].targetReps").value(8))
            .andExpect(jsonPath("$.content[0].finalRoutinePayload.routineBlocks[0].prescription[0].targetWeightKg").value(80));
    }
}
