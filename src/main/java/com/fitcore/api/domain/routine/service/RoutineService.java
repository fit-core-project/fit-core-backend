package com.fitcore.api.domain.routine.service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;
import com.fitcore.api.domain.routine.repository.RoutineDraftRepository;
import com.fitcore.api.domain.routine.repository.RoutineFinalRepository;
import com.fitcore.api.domain.routine.request.RoutineDraftRequest;
import com.fitcore.api.domain.routine.request.RoutineFinalRequest;
import com.fitcore.api.domain.routine.response.RoutineDraftResponse;
import com.fitcore.api.domain.routine.response.RoutineFinalResponse;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {
    private final RoutineDraftRepository draftRepository;
    private final RoutineFinalRepository finalRepository;
    private final SecurityUtils securityUtils;

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

        RoutineDraftEntity savedDraft = draftRepository.save(draft);
        return RoutineDraftResponse.fromEntity(savedDraft);
    }

    // 2. 루틴 확정 (Request -> Entity -> Response)
    @Transactional
    public RoutineFinalResponse finalizeRoutine(String routineDraftId, RoutineFinalRequest request) {
        request.setRoutineDraftId(routineDraftId);
        String currentUserId = securityUtils.getCurrentUserId();

        // Draft 조회 및 권한 검증
        RoutineDraftEntity draft = draftRepository.findById(request.getRoutineDraftId())
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

        RoutineFinalEntity savedFinal = finalRepository.save(finalEntity);
        return RoutineFinalResponse.fromEntity(savedFinal);
    }

    // 3. 내 확정 루틴 페이징 조회 (Entity Page -> Response Page 변환)
    public Page<RoutineFinalResponse> getMyFinalRoutines(Pageable pageable) {
        String userId = securityUtils.getCurrentUserId();

        // Entity 페이징 조회 후 Response DTO로 매핑하여 반환
        return finalRepository.findByUserId(userId, pageable)
            .map(RoutineFinalResponse::fromEntity);
    }

    public RoutineFinalResponse getFinalRoutine(String finalId) {
        String currentUserId = securityUtils.getCurrentUserId();

        // 1. 데이터 조회
        RoutineFinalEntity finalEntity = finalRepository.findById(finalId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 권한 체크 (본인의 루틴인지 확인)
        if (!finalEntity.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 3. DTO 변환 반환
        return RoutineFinalResponse.fromEntity(finalEntity);
    }
}
