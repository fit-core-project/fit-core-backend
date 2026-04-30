package com.fitcore.api.domain.routine.service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;
import com.fitcore.api.domain.routine.repository.RoutineDraftRepository;
import com.fitcore.api.domain.routine.repository.RoutineFinalRepository;
import com.fitcore.api.domain.routine.request.RoutineDraftRequest;
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
        String draftId = UUID.randomUUID().toString();

        // 1. 요청 데이터를 AI 서버 전용 DTO로 매핑
        AiRoutineRequest aiRequest = mapToAiRequest(request);

        try {
            // 2. AI 서버 호출
            AiRoutineResponse res = aiClient.generateRoutine(aiRequest);
            System.out.println(res);
            // 3. 성공 시 DB 저장 (Audit Trail)
            RoutineDraftEntity successEntity = RoutineDraftEntity.builder()
                .id(res.getRoutineDraftId())
                .userId(securityUtils.getCurrentUserId())
                .generationStatus(res.getGenerationStatus().name())
                .statusReasonCode(res.getStatusReasonCode().name())
                .targetSplitLabel(res.getSummaryTitle())
                .isFallback(Boolean.TRUE.equals(res.getIsFallback()))
                .requestPayloadSnapshot(objectMapper.convertValue(request, new TypeReference<>() {
                }))
                .responsePayloadSnapshot(objectMapper.convertValue(res, new TypeReference<>() {
                }))
                .rationaleSummary(res.getRationaleSummary())
                .build();

            return RoutineDraftResponse.fromEntity(routineDraftRepository.save(successEntity));

        } catch (Exception e) {
            e.printStackTrace();
            RoutineDraftEntity failedEntity = RoutineDraftEntity.builder()
                .id(draftId)
                .userId(securityUtils.getCurrentUserId())
                .generationStatus("FAILED")
                .statusReasonCode("AI_SERVER_ERROR")
                .requestPayloadSnapshot(objectMapper.convertValue(request, new TypeReference<>() {
                }))
                .rationaleSummary(Arrays.asList("Error"))
                .build();

            return RoutineDraftResponse.fromEntity(routineDraftRepository.save(failedEntity));
        }
    }

    private AiRoutineRequest mapToAiRequest(RoutineGenerateRequest request) {
        return AiRoutineRequest.builder()
            .userId(securityUtils.getCurrentUserId())
            .targetSplitLabel(request.getTargetSplitLabel())
            .targetMuscles(request.getTargetMuscles())
            .readinessLevel(request.getReadinessLevel())
            .timeAvailableMin(request.getTimeAvailableMin())
            .currentPainAreas(request.getCurrentPainAreas())
            .doms(convertDomsToList(request.getDoms()))
            .unavailableEquipment(request.getUnavailableEquipment()) // 예시: 전체 장비 리스트 등에서 제외하여 매핑
            .goal(request.getGoal())
            .userNote(request.getUserNote())
            .build();
    }

    private List<AiRoutineRequest.DomEntryDto> convertDomsToList(List<RoutineGenerateRequest.DomsRequest> doms) {
        if (doms == null || doms.isEmpty()) {
            return Collections.emptyList(); // 빈 리스트 반환
        }

        return doms.stream()
            .map(d -> AiRoutineRequest.DomEntryDto.builder()
                .bodyPart(d.getBodyPart())          // bodyPart -> muscle 매핑
                .level(d.getLevel())
                .build())
            .collect(Collectors.toList());
    }

    // 1. 루틴 초안 저장 (Request -> Entity -> Response)
    @Transactional
    public RoutineDraftResponse saveDraft(RoutineDraftRequest request) {
        String userId = securityUtils.getCurrentUserId();

        // DTO를 사용하여 Entity 생성
        RoutineDraftEntity draft = RoutineDraftEntity.builder()
            .userId(userId)
            .sourceProfileVersion(request.getSourceProfileVersion())
            .sourceWorkoutSessionIds(request.getSourceWorkoutSessionIds())
            .targetSplitLabel(request.getTargetSplitLabel())
            .requestPayloadSnapshot(request.getRequestPayloadSnapshot())
            // 시스템 내부 기본값 설정
            .generationStatus("PENDING")
            .statusReasonCode("CREATED")
            .isFallback(false)
            .createdAt(LocalDateTime.now())
            .build();

        RoutineDraftEntity savedDraft = routineDraftRepository.save(draft);
        return RoutineDraftResponse.fromEntity(savedDraft);
    }

    // 2. 루틴 확정 (Request -> Entity -> Response)
    @Transactional
    public RoutineFinalResponse finalizeRoutine(String routineDraftId, RoutineFinalRequest request) {
        request.setRoutineDraftId(routineDraftId);
        String currentUserId = securityUtils.getCurrentUserId();

        // Draft 조회 및 권한 검증
        RoutineDraftEntity draft = routineDraftRepository.findById(request.getRoutineDraftId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!draft.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // Final Entity 생성
        RoutineFinalEntity finalEntity = RoutineFinalEntity.builder()
            .id(UUID.randomUUID().toString())
            .routineDraft(draft)
            .userId(currentUserId)
            .targetWorkoutDate(request.getTargetWorkoutDate())
            .targetSplitLabel(request.getTargetSplitLabel())
            .finalRoutinePayload(draft.getResponsePayloadSnapshot()) // draft의 응답 스냅샷 활용
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
