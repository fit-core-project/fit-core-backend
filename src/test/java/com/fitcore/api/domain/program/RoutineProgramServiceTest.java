package com.fitcore.api.domain.program;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.program.entity.RoutineProgramEntity;
import com.fitcore.api.domain.program.entity.RoutineProgramStatus;
import com.fitcore.api.domain.program.exception.ProgramException;
import com.fitcore.api.domain.program.repository.RoutineProgramCompletionEventRepository;
import com.fitcore.api.domain.program.repository.RoutineProgramItemRepository;
import com.fitcore.api.domain.program.repository.RoutineProgramRepository;
import com.fitcore.api.domain.program.request.ProgramCreateRequest;
import com.fitcore.api.domain.program.response.ProgramDetailResponse;
import com.fitcore.api.domain.program.response.ProgramSummaryResponse;
import com.fitcore.api.domain.program.service.RoutineProgramService;
import com.fitcore.api.domain.routine.dto.FinalRoutinePayload;
import com.fitcore.api.domain.routine.dto.Prescription;
import com.fitcore.api.domain.routine.dto.RoutineBlock;
import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;
import com.fitcore.api.domain.routine.repository.RoutineDraftRepository;
import com.fitcore.api.domain.routine.repository.RoutineFinalRepository;
import com.fitcore.api.domain.workout.request.WorkoutSessionRequest;
import com.fitcore.api.domain.workout.request.WorkoutSetRequest;
import com.fitcore.api.domain.workout.service.WorkoutSessionService;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.exception.BusinessException;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoutineProgramServiceTest {
    @Autowired
    private RoutineProgramService programService;

    @Autowired
    private WorkoutSessionService workoutSessionService;

    @Autowired
    private RoutineDraftRepository routineDraftRepository;

    @Autowired
    private RoutineFinalRepository routineFinalRepository;

    @Autowired
    private RoutineProgramRepository programRepository;

    @Autowired
    private RoutineProgramItemRepository itemRepository;

    @Autowired
    private RoutineProgramCompletionEventRepository eventRepository;

    @MockitoBean
    private SecurityUtils securityUtils;

    @Test
    void createProgram_savesActiveProgramWithOneBasedItems() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("user-a", "pull", "Pull", 50);

        ProgramDetailResponse response = programService.createProgram(createRequest("PPL", push.getId(), pull.getId()));

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getCurrentPosition()).isEqualTo(1);
        assertThat(response.getItems()).extracting("position").containsExactly(1, 2);
        assertThat(response.getNextItem().getRoutineFinalId()).isEqualTo(push.getId());
    }

    @Test
    void createProgram_rejectsInvalidRoutineListsAndActiveConflict() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("user-a", "pull", "Pull", 50);

        assertThatThrownBy(() -> programService.createProgram(createRequest("Too short", push.getId())))
            .isInstanceOf(ProgramException.class);
        assertThatThrownBy(() -> programService.createProgram(createRequest("Duplicate", push.getId(), push.getId())))
            .isInstanceOf(ProgramException.class);

        RoutineFinalEntity otherUserRoutine = saveFinal("user-b", "legs", "Legs", 55);
        assertThatThrownBy(() -> programService.createProgram(createRequest("Other user", push.getId(), otherUserRoutine.getId())))
            .isInstanceOf(BusinessException.class);

        programService.createProgram(createRequest("PPL", push.getId(), pull.getId()));
        assertThatThrownBy(() -> programService.createProgram(createRequest("Second", push.getId(), pull.getId())))
            .isInstanceOf(ProgramException.class);
    }

    @Test
    void activeSummary_returnsCurrentItemAndIgnoresCompletedProgram() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("user-a", "pull", "Pull", 50);
        ProgramDetailResponse created = programService.createProgram(createRequest("PPL", push.getId(), pull.getId()));

        ProgramSummaryResponse active = programService.getActiveProgram().orElseThrow();
        assertThat(active.getNextItem().getRoutineFinalId()).isEqualTo(push.getId());
        assertThat(active.getNextItem().getEstimatedTime()).isEqualTo(45);

        saveWorkout(push.getId(), created.getProgramId(), created.getItems().get(0).getProgramItemId());
        saveWorkout(pull.getId(), created.getProgramId(), created.getItems().get(1).getProgramItemId());

        assertThat(programService.getActiveProgram()).isEmpty();
    }

    @Test
    void detailComputesDoneCurrentAndUpcomingStates() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("user-a", "pull", "Pull", 50);
        RoutineFinalEntity legs = saveFinal("user-a", "legs", "Legs", 55);
        ProgramDetailResponse created = programService.createProgram(createRequest("PPL", push.getId(), pull.getId(), legs.getId()));

        saveWorkout(push.getId(), created.getProgramId(), created.getItems().get(0).getProgramItemId());

        ProgramDetailResponse detail = programService.getProgram(created.getProgramId());
        assertThat(detail.getCurrentPosition()).isEqualTo(2);
        assertThat(detail.getItems()).extracting("state").containsExactly("DONE", "CURRENT", "UPCOMING");
    }

    @Test
    void getProgram_rejectsOtherUserProgram() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("user-a", "pull", "Pull", 50);
        ProgramDetailResponse created = programService.createProgram(createRequest("PPL", push.getId(), pull.getId()));

        when(securityUtils.getCurrentUserId()).thenReturn("user-b");
        assertThatThrownBy(() -> programService.getProgram(created.getProgramId()))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void workoutSaveWithoutProgramContextKeepsExistingBehavior() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);

        var response = saveWorkout(push.getId(), null, null);

        assertThat(response.getSourceRoutineFinalId()).isEqualTo(push.getId());
        assertThat(eventRepository.findAll()).isEmpty();
    }

    @Test
    void workoutSaveWithProgramContextAdvancesCursorAndCompletesLastItem() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("user-a", "pull", "Pull", 50);
        ProgramDetailResponse created = programService.createProgram(createRequest("PPL", push.getId(), pull.getId()));

        saveWorkout(push.getId(), created.getProgramId(), created.getItems().get(0).getProgramItemId());
        RoutineProgramEntity afterFirst = programRepository.findById(created.getProgramId()).orElseThrow();
        assertThat(afterFirst.getCurrentPosition()).isEqualTo(2);
        assertThat(afterFirst.getStatus()).isEqualTo(RoutineProgramStatus.ACTIVE);

        saveWorkout(pull.getId(), created.getProgramId(), created.getItems().get(1).getProgramItemId());
        RoutineProgramEntity afterLast = programRepository.findById(created.getProgramId()).orElseThrow();
        assertThat(afterLast.getCurrentPosition()).isEqualTo(2);
        assertThat(afterLast.getStatus()).isEqualTo(RoutineProgramStatus.COMPLETED);
        assertThat(afterLast.getCompletedAt()).isNotNull();
    }

    @Test
    void workoutSaveRejectsMismatchedProgramContext() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-a");
        RoutineFinalEntity push = saveFinal("user-a", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("user-a", "pull", "Pull", 50);
        ProgramDetailResponse created = programService.createProgram(createRequest("PPL", push.getId(), pull.getId()));

        assertThatThrownBy(() -> saveWorkout(push.getId(), created.getProgramId(), created.getItems().get(1).getProgramItemId()))
            .isInstanceOf(ProgramException.class);
        assertThatThrownBy(() -> saveWorkout(pull.getId(), created.getProgramId(), created.getItems().get(0).getProgramItemId()))
            .isInstanceOf(ProgramException.class);
        assertThatThrownBy(() -> saveWorkout(push.getId(), created.getProgramId(), null))
            .isInstanceOf(ProgramException.class);

        RoutineProgramEntity program = programRepository.findById(created.getProgramId()).orElseThrow();
        assertThat(program.getCurrentPosition()).isEqualTo(1);
    }

    private com.fitcore.api.domain.workout.response.WorkoutSessionResponse saveWorkout(
        String routineFinalId,
        String programId,
        String programItemId
    ) {
        WorkoutSessionRequest request = new WorkoutSessionRequest();
        request.setWorkoutDate(LocalDate.of(2026, 5, 18));
        request.setSplitLabel("push");
        request.setSourceRoutineFinalId(routineFinalId);
        request.setProgramId(programId);
        request.setProgramItemId(programItemId);
        request.setReadinessLevel("normal");
        request.setCurrentPainAreas(List.of());
        request.setCurrentDoms(List.of());
        request.setUnavailableEquipment(List.of());
        request.setSets(List.of(workoutSet()));
        return workoutSessionService.createWorkoutSession(request);
    }

    private WorkoutSetRequest workoutSet() {
        WorkoutSetRequest set = new WorkoutSetRequest();
        set.setExerciseOrder(1);
        set.setExerciseId("barbell_bench_press");
        set.setExerciseNameSnapshot("Barbell Bench Press");
        set.setSetIndex(1);
        set.setSetType("working");
        set.setTrackingMode("weightReps");
        set.setWeightKg(BigDecimal.valueOf(70));
        set.setReps(8);
        set.setRir(BigDecimal.valueOf(2));
        set.setIsFailure(false);
        set.setRestSec(120);
        return set;
    }

    private ProgramCreateRequest createRequest(String name, String... routineFinalIds) {
        ProgramCreateRequest request = new ProgramCreateRequest();
        request.setName(name);
        request.setRoutineFinalIds(List.of(routineFinalIds));
        return request;
    }

    private RoutineFinalEntity saveFinal(String userId, String split, String title, int estimatedTime) {
        RoutineDraftEntity draft = routineDraftRepository.save(RoutineDraftEntity.builder()
            .userId(userId)
            .sourceProfileVersion(1)
            .targetSplitLabel(split)
            .generationStatus(GenerationStatus.success)
            .statusReasonCode(StatusReasonCode.none)
            .isFallback(false)
            .requestPayloadSnapshot(new com.fitcore.api.domain.routine.request.RoutineGenerateRequest())
            .responsePayloadSnapshot(new com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse())
            .rationaleSummary(List.of())
            .build());

        return routineFinalRepository.save(RoutineFinalEntity.builder()
            .routineDraft(draft)
            .userId(userId)
            .targetWorkoutDate(LocalDate.of(2026, 5, 18))
            .targetSplitLabel(split)
            .finalRoutinePayload(payload(title, estimatedTime))
            .acceptedWithoutEdits(true)
            .userEditSummary(List.of())
            .savedAt(LocalDateTime.now())
            .build());
    }

    private FinalRoutinePayload payload(String title, int estimatedTime) {
        Prescription prescription = new Prescription();
        prescription.setSetIndex(1);
        prescription.setSetType("working");
        prescription.setTargetReps(8);
        prescription.setTargetRestSec(120);

        RoutineBlock block = new RoutineBlock();
        block.setOrder(1);
        block.setExerciseId("barbell_bench_press");
        block.setExerciseName("Barbell Bench Press");
        block.setPrimaryMuscles(List.of("chest"));
        block.setDefaultRestSec(120);
        block.setPrescription(List.of(prescription));

        FinalRoutinePayload payload = new FinalRoutinePayload();
        payload.setFallback(false);
        payload.setGenerationStatus(GenerationStatus.success);
        payload.setStatusReasonCode(StatusReasonCode.none);
        payload.setSummaryTitle(title);
        payload.setTotalEstimatedTime(estimatedTime);
        payload.setRationaleSummary(List.of("test"));
        payload.setWarnings(List.of());
        payload.setRoutineBlocks(List.of(block));
        return payload;
    }
}
