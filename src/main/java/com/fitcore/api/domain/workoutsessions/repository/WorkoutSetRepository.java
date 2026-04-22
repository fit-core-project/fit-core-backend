package com.fitcore.api.domain.workoutsessions.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.workoutsessions.entity.WorkoutSetEntity;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSetEntity, String> {
    
}
