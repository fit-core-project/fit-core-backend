package com.fitcore.api.domain.workout.components;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;
import com.fitcore.api.domain.workout.repository.WorkoutSetRepository;

@Component
@RequiredArgsConstructor
public class WorkoutSessionComponents {
    private final WorkoutSetRepository workoutSetRepository;

    public Optional<WorkoutSetEntity> findLatestByUserAndExercise(String userId, String exerciseId) {
        return workoutSetRepository.findLatestByUserAndExercise(userId, exerciseId);
    }
}
