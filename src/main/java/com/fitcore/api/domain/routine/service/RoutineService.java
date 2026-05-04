package com.fitcore.api.domain.routine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.domain.routine.dto.Doms;
import com.fitcore.api.domain.routine.dto.Prescription;
import com.fitcore.api.domain.routine.dto.RoutineBlock;
import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;
import com.fitcore.api.domain.routine.repository.RoutineDraftRepository;
import com.fitcore.api.domain.routine.repository.RoutineFinalRepository;
import com.fitcore.api.domain.routine.request.RoutineFinalRequest;
import com.fitcore.api.domain.routine.request.RoutineGenerateRequest;
import com.fitcore.api.domain.routine.response.RoutineDraftResponse;
import com.fitcore.api.domain.routine.response.RoutineFinalResponse;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;
import com.fitcore.api.infrastructure.ai.client.AiClient;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineRequest;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {
    private final RoutineDraftRepository routineDraftRepository;
    private final RoutineFinalRepository routineFinalRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;
    private final AiClient aiClient;

    @Transactional
    public RoutineDraftResponse generateRoutine(RoutineGenerateRequest request) {
        log.info("AI Routine Generate UserId: {} Request: {}", securityUtils.getCurrentUserId(), request);

        try {
            AiRoutineRequest aiRequest = mapToAiRequest(request);
            AiRoutineResponse res = aiClient.generateRoutine(aiRequest);
            res.setIsFallback(Boolean.TRUE.equals(res.getIsFallback()));
            log.info("AI Response: {}", res);

            RoutineDraftEntity successEntity = RoutineDraftEntity.builder()
                .userId(securityUtils.getCurrentUserId())
                .generationStatus(res.getGenerationStatus())
                .statusReasonCode(res.getStatusReasonCode())
                .targetSplitLabel(res.getSummaryTitle())
                .isFallback(res.getIsFallback())
                .requestPayloadSnapshot(objectMapper.convertValue(request, new TypeReference<>() {
                }))
                .responsePayloadSnapshot(objectMapper.convertValue(res, new TypeReference<>() {
                }))
                .rationaleSummary(res.getRationaleSummary())
                .build();

            return RoutineDraftResponse.fromEntity(routineDraftRepository.save(successEntity));
        } catch (Exception e) {
            log.error(e.toString());
            RoutineDraftEntity aiFailEntity = RoutineDraftEntity.builder()
                .userId(securityUtils.getCurrentUserId())
                .generationStatus(GenerationStatus.fallback)
                .statusReasonCode(StatusReasonCode.llmTimeout)
                .targetSplitLabel("기본 push 루틴") // 디폴트 응답 타이틀
                .isFallback(true)
                .requestPayloadSnapshot(objectMapper.convertValue(request, new TypeReference<>() {
                }))
                .responsePayloadSnapshot(
                    objectMapper.convertValue(createAiFailResponse(), new TypeReference<>() { // 디폴트 루틴
                    }))
                .rationaleSummary(List.of("LLM 응답이 제한 시간 안에 오지 않아 규칙 기반 기본 루틴으로 전환했다.")) // 디폴트
                .build();

            return RoutineDraftResponse.fromEntity(routineDraftRepository.save(aiFailEntity));
        }
    }

    private AiRoutineResponse createAiFailResponse() {
        List<Prescription> prescriptions = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            Prescription p = new Prescription();
            p.setSetIndex(i);
            p.setSetType("working");
            p.setTargetReps(5);
            p.setTargetWeightKg(BigDecimal.valueOf(75));
            p.setTargetRir(2);
            p.setTargetRestSec(120);
            prescriptions.add(p);
        }

        // 2. RoutineBlock 생성 및 값 설정
        RoutineBlock benchPressBlock = getRoutineBlock(prescriptions);

        // 3. 최종 응답 객체 생성 (AiRoutineResponse의 구조에 따라 적절히 반환)
        List<RoutineBlock> blocks = new ArrayList<>();
        blocks.add(benchPressBlock);

        AiRoutineResponse response = new AiRoutineResponse();
        response.setRoutineBlocks(blocks); // 필드명이 routineBlocks라고 가정

        return response;
    }

    private static @NonNull RoutineBlock getRoutineBlock(List<Prescription> prescriptions) {
        RoutineBlock benchPressBlock = new RoutineBlock();
        benchPressBlock.setOrder(1);
        benchPressBlock.setExerciseId("barbell_bench_press");
        benchPressBlock.setExerciseName("Barbell Bench Press");
        benchPressBlock.setMovementPattern("horizontalPush");
        benchPressBlock.setPrimaryMuscles(Arrays.asList("chest", "triceps"));
        benchPressBlock.setEquipmentType("barbell");
        benchPressBlock.setDefaultRestSec(120);
        benchPressBlock.setPrescription(prescriptions);
        benchPressBlock.setExerciseRationale("최근 수행 성공 기록을 기준으로 마지막 확인 중량 유지");
        benchPressBlock.setSubstitutionCandidates(new ArrayList<>());
        return benchPressBlock;
    }

    private AiRoutineRequest mapToAiRequest(RoutineGenerateRequest request) {
        return AiRoutineRequest.builder()
            .userId(securityUtils.getCurrentUserId())
            .targetSplitLabel(request.getTargetSplitLabel())
            .targetMuscles(request.getTargetMuscles())
            .readinessLevel(request.getReadinessLevel())
            .timeAvailableMin(request.getTimeAvailableMin())
            .currentPainAreas(request.getCurrentPainAreas())
            .doms(convertDomsToList(request.getCurrentDoms()))
            .unavailableEquipment(request.getUnavailableEquipment()) // 예시: 전체 장비 리스트 등에서 제외하여 매핑
            .goal(request.getGoal())
            .userNote(request.getUserNote())
            .build();
    }

    private List<Doms> convertDomsToList(List<Doms> doms) {
        if (doms == null || doms.isEmpty()) {
            return Collections.emptyList(); // 빈 리스트 반환
        }

        return doms.stream()
            .map(d -> Doms.builder()
                .bodyPart(d.getBodyPart())          // bodyPart -> muscle 매핑
                .level(d.getLevel())
                .build())
            .collect(Collectors.toList());
    }

    // 2. 루틴 확정 (Request -> Entity -> Response)
    @Transactional
    public RoutineFinalResponse finalizeRoutine(String routineDraftId, RoutineFinalRequest request) {
        String currentUserId = securityUtils.getCurrentUserId();

        // Draft 조회 및 권한 검증
        RoutineDraftEntity draft = routineDraftRepository.findById(routineDraftId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!draft.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // Final Entity 생성
        RoutineFinalEntity finalEntity = RoutineFinalEntity.builder()
            .routineDraft(draft)
            .userId(currentUserId)
            .targetWorkoutDate(request.getTargetWorkoutDate())
            .targetSplitLabel(draft.getTargetSplitLabel())
            .finalRoutinePayload(request.getFinalRoutinePayload())
            .acceptedWithoutEdits(request.getAcceptedWithoutEdits())
            .userEditSummary(request.getUserEditSummary())
            .savedAt(LocalDateTime.now())
            .build();

        RoutineFinalEntity savedFinal = routineFinalRepository.save(finalEntity);
        return RoutineFinalResponse.fromEntity(savedFinal);
    }

    // 3. 내 확정 루틴 페이징 조회 (Entity Page -> Response Page 변환)
    public Page<RoutineFinalResponse> getMyFinalRoutines(Pageable pageable) {
        String userId = securityUtils.getCurrentUserId();

        // Entity 페이징 조회 후 Response DTO로 매핑하여 반환
        return routineFinalRepository.findByUserId(userId, pageable)
            .map(RoutineFinalResponse::fromEntity);
    }

    public RoutineFinalResponse getFinalRoutine(String finalId) {
        String currentUserId = securityUtils.getCurrentUserId();

        // 1. 데이터 조회
        RoutineFinalEntity finalEntity = routineFinalRepository.findById(finalId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 권한 체크 (본인의 루틴인지 확인)
        if (!finalEntity.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 3. DTO 변환 반환
        return RoutineFinalResponse.fromEntity(finalEntity);
    }
}
