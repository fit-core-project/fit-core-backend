package com.fitcore.api.domain.workout.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSetEntity, String> {

}
