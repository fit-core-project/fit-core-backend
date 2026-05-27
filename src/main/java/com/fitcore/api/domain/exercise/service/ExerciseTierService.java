package com.fitcore.api.domain.exercise.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.exercise.entity.ExerciseTierEntity;
import com.fitcore.api.domain.exercise.repository.ExerciseTierRepository;
import com.fitcore.api.domain.exercise.request.ExerciseTierRequest;
import com.fitcore.api.domain.exercise.response.AdminExerciseTierResponse;
import com.fitcore.api.domain.exercise.response.ExerciseTierResponse;
import com.fitcore.api.domain.exercise.response.RecentRecordResponse;
import com.fitcore.api.domain.workout.components.WorkoutSessionComponents;
import com.fitcore.api.global.common.util.SecurityUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseTierService {
    private final SecurityUtils securityUtils;
    private final ExerciseTierRepository exerciseTierRepository;
    private final WorkoutSessionComponents workoutSessionComponents;

    public List<ExerciseTierResponse> getAllExerciseTiers() {
        return exerciseTierRepository.findAll().stream()
            .map(ExerciseTierResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public Optional<RecentRecordResponse> getRecentRecord(String exerciseId) {
        return workoutSessionComponents.findLatestByUserAndExercise(securityUtils.getCurrentUserId(), exerciseId)
            .map(RecentRecordResponse::fromEntity);
    }

    @Transactional
    public AdminExerciseTierResponse createExercise(ExerciseTierRequest req) {
        ExerciseTierEntity entity = ExerciseTierEntity.builder()
            .nameKr(req.getNameKr())
            .nameEn(req.getNameEn())
            .primaryMuscle(req.getPrimaryMuscle())
            .secondaryMuscle(req.getSecondaryMuscle())
            .equipmentReq(req.getEquipmentReq())
            .difficultyTier(req.getDifficultyTier())
            .efficiencyTier(req.getEfficiencyTier())
            .painTriggers(req.getPainTriggers())
            .movementType(req.getMovementType())
            .substituteExerciseIds(normalizeSubstituteIds(req.getSubstituteExerciseIds(), null))
            .build();
        return AdminExerciseTierResponse.fromEntity(exerciseTierRepository.save(entity));
    }

    @Transactional
    public AdminExerciseTierResponse updateExercise(Long id, ExerciseTierRequest req) {
        ExerciseTierEntity entity = exerciseTierRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + id));
        entity.update(req.getNameKr(), req.getNameEn(),
            req.getPrimaryMuscle(), req.getSecondaryMuscle(),
            req.getEquipmentReq(), req.getDifficultyTier(), req.getEfficiencyTier(),
            req.getPainTriggers(), req.getMovementType(),
            normalizeSubstituteIds(req.getSubstituteExerciseIds(), id));
        return AdminExerciseTierResponse.fromEntity(entity);
    }

    @Transactional
    public void deleteExercise(Long id) {
        String deletedId = String.valueOf(id);
        List<ExerciseTierEntity> affected = exerciseTierRepository.findAll().stream()
            .filter(exercise -> containsSubstituteId(exercise.getSubstituteExerciseIds(), deletedId))
            .toList();
        affected.forEach(exercise ->
            exercise.setSubstituteExerciseIds(removeSubstituteId(exercise.getSubstituteExerciseIds(), deletedId))
        );
        exerciseTierRepository.deleteById(id);
    }

    public List<AdminExerciseTierResponse> getAllExercisesForAdmin() {
        return exerciseTierRepository.findAll().stream()
            .map(AdminExerciseTierResponse::fromEntity)
            .collect(Collectors.toList());
    }

    private String normalizeSubstituteIds(String raw, Long selfId) {
        String self = selfId == null ? null : String.valueOf(selfId);
        return Stream.of(Optional.ofNullable(raw).orElse("").split(","))
            .map(String::trim)
            .filter(token -> !token.isBlank())
            .filter(token -> self == null || !token.equals(self))
            .distinct()
            .collect(Collectors.joining(", "));
    }

    private boolean containsSubstituteId(String raw, String id) {
        return Stream.of(Optional.ofNullable(raw).orElse("").split(","))
            .map(String::trim)
            .anyMatch(id::equals);
    }

    private String removeSubstituteId(String raw, String id) {
        return Stream.of(Optional.ofNullable(raw).orElse("").split(","))
            .map(String::trim)
            .filter(token -> !token.isBlank())
            .filter(token -> !token.equals(id))
            .distinct()
            .collect(Collectors.joining(", "));
    }
}
