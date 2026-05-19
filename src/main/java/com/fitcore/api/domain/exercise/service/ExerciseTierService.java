package com.fitcore.api.domain.exercise.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.exercise.repository.ExerciseTierRepository;
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
}
