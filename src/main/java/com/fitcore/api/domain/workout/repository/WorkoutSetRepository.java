package com.fitcore.api.domain.workout.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSetEntity, String> {
    @Query("SELECT ws FROM WorkoutSetEntity ws " +
           "JOIN ws.workoutSession s " +
           "WHERE s.userId = :userId " +
           "AND ws.exerciseId = :exerciseId " +
           "ORDER BY ws.createdAt DESC LIMIT 1")
    Optional<WorkoutSetEntity> findLatestByUserAndExercise(
        @Param("userId") String userId,
        @Param("exerciseId") String exerciseId
    );

    @Query("SELECT ws FROM WorkoutSetEntity ws JOIN FETCH ws.workoutSession s " +
           "WHERE s.userId = :userId AND ws.setType = 'working' " +
           "AND ws.weightKg IS NOT NULL AND ws.reps > 0")
    List<WorkoutSetEntity> findWorkingSetsWithWeightByUser(@Param("userId") String userId);
}
