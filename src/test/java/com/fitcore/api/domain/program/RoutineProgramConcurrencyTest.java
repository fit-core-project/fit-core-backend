package com.fitcore.api.domain.program;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fitcore.api.domain.program.entity.RoutineProgramStatus;
import com.fitcore.api.domain.program.exception.ProgramException;
import com.fitcore.api.domain.program.repository.RoutineProgramRepository;
import com.fitcore.api.domain.program.request.ProgramCreateRequest;
import com.fitcore.api.domain.program.service.RoutineProgramService;
import com.fitcore.api.domain.routine.dto.FinalRoutinePayload;
import com.fitcore.api.domain.routine.dto.Prescription;
import com.fitcore.api.domain.routine.dto.RoutineBlock;
import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;
import com.fitcore.api.domain.routine.repository.RoutineDraftRepository;
import com.fitcore.api.domain.routine.repository.RoutineFinalRepository;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@SpringBootTest
@ActiveProfiles("test")
class RoutineProgramConcurrencyTest {

    @Autowired
    private RoutineProgramService programService;

    @Autowired
    private RoutineProgramRepository programRepository;

    @Autowired
    private RoutineDraftRepository routineDraftRepository;

    @Autowired
    private RoutineFinalRepository routineFinalRepository;

    @MockitoBean
    private SecurityUtils securityUtils;

    @Test
    void concurrentCreateProgram_allowsExactlyOneActive() throws InterruptedException {
        when(securityUtils.getCurrentUserId()).thenReturn("concurrent-user");

        RoutineFinalEntity push = saveFinal("concurrent-user", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("concurrent-user", "pull", "Pull", 50);

        int threads = 5;
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger conflicts = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    programService.createProgram(createRequest("Program-" + idx, push.getId(), pull.getId()));
                    successes.incrementAndGet();
                } catch (ProgramException e) {
                    conflicts.incrementAndGet();
                } catch (Exception ignored) {
                    conflicts.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        long activeCount = programRepository.findByUserIdOrderByCreatedAtDesc("concurrent-user").stream()
            .filter(p -> p.getStatus() == RoutineProgramStatus.ACTIVE)
            .count();

        assertThat(activeCount).isEqualTo(1);
        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(threads - 1);
    }

    @Test
    void concurrentActivateProgram_allowsExactlyOneActive() throws InterruptedException {
        when(securityUtils.getCurrentUserId()).thenReturn("activate-user");

        RoutineFinalEntity push = saveFinal("activate-user", "push", "Push", 45);
        RoutineFinalEntity pull = saveFinal("activate-user", "pull", "Pull", 50);

        int threads = 5;
        List<String> archivedProgramIds = new java.util.ArrayList<>();
        for (int i = 0; i < threads; i++) {
            var created = programService.createProgram(createRequest("Archived-" + i, push.getId(), pull.getId()));
            programService.archiveProgram(created.getProgramId());
            archivedProgramIds.add(created.getProgramId());
        }

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger conflicts = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (String programId : archivedProgramIds) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    programService.activateProgram(programId);
                    successes.incrementAndGet();
                } catch (ProgramException e) {
                    conflicts.incrementAndGet();
                } catch (Exception ignored) {
                    conflicts.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        long activeCount = programRepository.findByUserIdOrderByCreatedAtDesc("activate-user").stream()
            .filter(p -> p.getStatus() == RoutineProgramStatus.ACTIVE)
            .count();

        assertThat(activeCount).isEqualTo(1);
        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(threads - 1);
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

        FinalRoutinePayload fp = new FinalRoutinePayload();
        fp.setFallback(false);
        fp.setGenerationStatus(GenerationStatus.success);
        fp.setStatusReasonCode(StatusReasonCode.none);
        fp.setSummaryTitle(title);
        fp.setTotalEstimatedTime(estimatedTime);
        fp.setRationaleSummary(List.of("test"));
        fp.setWarnings(List.of());
        fp.setRoutineBlocks(List.of(block));
        return fp;
    }
}
