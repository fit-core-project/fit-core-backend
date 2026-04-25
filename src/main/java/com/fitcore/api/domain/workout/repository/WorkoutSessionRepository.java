package com.fitcore.api.domain.workout.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.workout.entity.WorkoutSessionEntity;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSessionEntity, String> {
    Page<WorkoutSessionEntity> findByUserId(String userId, Pageable pageable);
}
