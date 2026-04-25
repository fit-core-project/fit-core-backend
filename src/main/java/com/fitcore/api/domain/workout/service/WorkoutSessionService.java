package com.fitcore.api.domain.workout.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.workout.entity.WorkoutSessionEntity;
import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;
import com.fitcore.api.domain.workout.repository.WorkoutSessionRepository;
import com.fitcore.api.domain.workout.request.WorkoutSessionRequest;
import com.fitcore.api.domain.workout.response.WorkoutSessionResponse;
import com.fitcore.api.global.common.util.SecurityUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionService {
    private final SecurityUtils securityUtils;
    private final WorkoutSessionRepository sessionRepository;

    /**
     * 운동 세션 및 세트 생성
     */
    @Transactional
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest request) {
        String userId = securityUtils.getCurrentUserId();

        log.info("Creating workout session for user: {}", userId);

        // 1. 세션 엔티티 빌드
        WorkoutSessionEntity session = WorkoutSessionEntity.builder()
            .userId(userId)
            .workoutDate(request.getWorkoutDate())
            .splitLabel(request.getSplitLabel())
            .sessionNote(request.getSessionNote())
            .currentPainAreas(request.getCurrentPainAreas())
            .doms(request.getDoms())
            .unavailableEquipment(request.getUnavailableEquipment())
            .build();

        // 2. 세트 엔티티 리스트 변환 및 연관관계 설정
        List<WorkoutSetEntity> sets = request.getSets().stream()
            .map(dto -> WorkoutSetEntity.builder()
                .workoutSession(session) // 연관관계 설정
                .exerciseOrder(dto.getExerciseOrder())
                .exerciseId(dto.getExerciseId())
                .exerciseNameSnapshot(dto.getExerciseNameSnapshot())
                .setIndex(dto.getSetIndex())
                .setType(dto.getSetType())
                .trackingMode(dto.getTrackingMode())
                .weightKg(dto.getWeightKg())
                .reps(dto.getReps())
                .rpe(dto.getRpe())
                .rir(dto.getRir())
                .isFailure(dto.getIsFailure())
                .restSec(dto.getRestSec())
                .setNote(dto.getSetNote())
                .build())
            .toList();

        session.getWorkoutSets().addAll(sets);

        // 3. 저장 (Cascade 설정으로 인해 세션만 저장해도 세트가 함께 저장됨)
        WorkoutSessionEntity savedSession = sessionRepository.save(session);

        return WorkoutSessionResponse.fromEntity(savedSession);
    }

    /**
     * 사용자별 운동 세션 페이징 조회
     */
    public Page<WorkoutSessionResponse> getWorkoutSessions(Pageable pageable) {
        String userId = securityUtils.getCurrentUserId();
        log.info("Fetching workout sessions for user: {}, page: {}", userId, pageable.getPageNumber());

        return sessionRepository.findByUserId(userId, pageable)
            .map(WorkoutSessionResponse::fromEntity);
    }

    /**
     * 세션 상세 조회
     */
    public WorkoutSessionResponse getWorkoutSessionById(String sessionId) {
        return sessionRepository.findById(sessionId)
            .map(WorkoutSessionResponse::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    /**
     * 세션 삭제
     */
    @Transactional
    public void deleteWorkoutSession(String sessionId) {
        log.info("Deleting workout session: {}", sessionId);
        if (!sessionRepository.existsById(sessionId)) {
            throw new IllegalArgumentException("Session not found");
        }
        sessionRepository.deleteById(sessionId);
    }
}
