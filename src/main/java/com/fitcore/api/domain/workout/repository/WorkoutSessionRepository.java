package com.fitcore.api.domain.workout.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fitcore.api.domain.workout.entity.WorkoutSessionEntity;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSessionEntity, String> {
    Page<WorkoutSessionEntity> findByUserId(String userId, Pageable pageable);

    @Query("SELECT DISTINCT s.workoutDate FROM WorkoutSessionEntity s " +
           "WHERE s.userId = :userId AND s.workoutDate >= :since " +
           "ORDER BY s.workoutDate ASC")
    List<LocalDate> findDistinctWorkoutDatesSince(
        @Param("userId") String userId,
        @Param("since") LocalDate since
    );
}
