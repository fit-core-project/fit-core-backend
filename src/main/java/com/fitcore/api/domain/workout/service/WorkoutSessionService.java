package com.fitcore.api.domain.workout.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.exercise.entity.ExerciseTierEntity;
import com.fitcore.api.domain.exercise.repository.ExerciseTierRepository;
import com.fitcore.api.domain.program.service.RoutineProgramService;
import com.fitcore.api.domain.routine.dto.Doms;
import com.fitcore.api.domain.user.components.UserComponent;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.repository.UserRepository;
import com.fitcore.api.domain.workout.entity.WorkoutSessionEntity;
import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;
import com.fitcore.api.domain.workout.repository.WorkoutSessionRepository;
import com.fitcore.api.domain.workout.repository.WorkoutSetRepository;
import com.fitcore.api.domain.workout.request.WorkoutSessionRequest;
import com.fitcore.api.domain.workout.response.AttendanceWeekResponse;
import com.fitcore.api.domain.workout.response.PrResponse;
import com.fitcore.api.domain.workout.response.WorkoutSessionResponse;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;
import com.fitcore.api.domain.routine.repository.RoutineFinalRepository;
import com.fitcore.api.domain.workout.util.OneRmCalculator;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionService {
    private final SecurityUtils securityUtils;
    private final WorkoutSessionRepository sessionRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final RoutineFinalRepository routineFinalRepository;
    private final UserComponent userComponent;
    private final ExerciseTierRepository exerciseTierRepository;
    private final UserRepository userRepository;
    private final RoutineProgramService routineProgramService;

    /**
     * 운동 세션 및 세트 생성
     */
    @Transactional
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest request) {
        String userId = securityUtils.getCurrentUserId();

        log.info("Creating workout session for user: {} request: {}", userId, request);

        validateSourceRoutineFinal(request.getSourceRoutineFinalId(), userId);

        // 1. 세션 엔티티 빌드
        WorkoutSessionEntity session = WorkoutSessionEntity.builder()
            .userId(userId)
            .workoutDate(request.getWorkoutDate())
            .splitLabel(request.getSplitLabel())
            .sourceRoutineFinalId(request.getSourceRoutineFinalId())
            .timeAvailableMin(request.getTimeAvailableMin())
            .durationMin(request.getDurationMin())
            .readinessLevel(request.getReadinessLevel())
            .currentPainAreas(request.getCurrentPainAreas())
            .doms(request.getCurrentDoms())
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

        session.setWorkoutSets(sets);

        // 3. 저장 (Cascade 설정으로 인해 세션만 저장해도 세트가 함께 저장됨)
        WorkoutSessionEntity savedSession = sessionRepository.save(session);

        routineProgramService.completeCurrentItemAfterWorkout(
            request.getProgramId(),
            request.getProgramItemId(),
            request.getSourceRoutineFinalId(),
            savedSession,
            userId
        );

        // 4. 운동한 근육을 DOMS level 2(moderate)로 기록
        updateDomsAfterWorkout(userId, request);

        return WorkoutSessionResponse.fromEntity(savedSession);
    }

    private void updateDomsAfterWorkout(String userId, WorkoutSessionRequest request) {
        Set<Long> exerciseIds = request.getSets().stream()
            .map(s -> {
                try { return Long.parseLong(s.getExerciseId()); }
                catch (NumberFormatException e) { return null; }
            })
            .filter(id -> id != null)
            .collect(Collectors.toSet());

        if (exerciseIds.isEmpty()) return;

        Set<String> workedMuscles = exerciseTierRepository.findAllById(exerciseIds).stream()
            .map(ExerciseTierEntity::getPrimaryMuscle)
            .filter(m -> m != null && !m.isBlank())
            .collect(Collectors.toSet());

        if (workedMuscles.isEmpty()) return;

        userRepository.findById(userId).ifPresent(user -> {
            LocalDate today = LocalDate.now();
            Map<String, Doms> domsMap = new LinkedHashMap<>();

            List<Doms> existing = user.getDoms();
            if (existing != null) {
                existing.forEach(d -> domsMap.put(d.getBodyPart(), d));
            }

            workedMuscles.forEach(muscle ->
                domsMap.put(muscle, Doms.builder()
                    .bodyPart(muscle)
                    .level("moderate")
                    .recordedAt(today)
                    .build())
            );

            user.updateDoms(new ArrayList<>(domsMap.values()));
            log.info("DOMS updated for user={} muscles={}", userId, workedMuscles);
        });
    }

    private void validateSourceRoutineFinal(String sourceRoutineFinalId, String userId) {
        if (sourceRoutineFinalId == null || sourceRoutineFinalId.isBlank()) {
            return;
        }
        RoutineFinalEntity finalRoutine = routineFinalRepository.findById(sourceRoutineFinalId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ROUTINE_NOT_FOUND));
        if (!finalRoutine.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
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

    public List<PrResponse> getPrs() {
        String userId = securityUtils.getCurrentUserId();
        List<WorkoutSetEntity> sets = workoutSetRepository.findWorkingSetsWithWeightByUser(userId);

        Map<String, PrResponse> bestByExercise = new LinkedHashMap<>();
        for (WorkoutSetEntity set : sets) {
            double estimated1RM = OneRmCalculator.estimate(set.getWeightKg().doubleValue(), set.getReps());
            PrResponse existing = bestByExercise.get(set.getExerciseId());
            if (existing == null || estimated1RM > existing.getEstimated1RM().doubleValue()) {
                bestByExercise.put(set.getExerciseId(), PrResponse.builder()
                    .exerciseId(set.getExerciseId())
                    .exerciseNameSnapshot(set.getExerciseNameSnapshot())
                    .estimated1RM(BigDecimal.valueOf(estimated1RM).setScale(1, RoundingMode.HALF_UP))
                    .weightKg(set.getWeightKg())
                    .reps(set.getReps())
                    .achievedDate(set.getWorkoutSession().getWorkoutDate())
                    .build());
            }
        }

        return bestByExercise.values().stream()
            .sorted(Comparator.comparingDouble(r -> -r.getEstimated1RM().doubleValue()))
            .toList();
    }

    public List<AttendanceWeekResponse> getAttendance() {
        String userId = securityUtils.getCurrentUserId();
        LocalDate thisMonday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate since = thisMonday.minusWeeks(3);

        List<LocalDate> workoutDates = sessionRepository.findDistinctWorkoutDatesSince(userId, since);
        Integer targetDays = userComponent.findById()
            .map(UserProfileEntity::getTrainingDaysPerWeek)
            .orElse(null);

        List<AttendanceWeekResponse> result = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            LocalDate weekStart = thisMonday.minusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            int actualDays = (int) workoutDates.stream()
                .filter(d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd))
                .count();
            Double rate = (targetDays != null && targetDays > 0)
                ? (double) actualDays / targetDays
                : null;
            result.add(AttendanceWeekResponse.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .actualDays(actualDays)
                .targetDays(targetDays)
                .rate(rate)
                .build());
        }
        return result;
    }
}
