package com.fitcore.api.domain.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitcore.api.domain.exercise.entity.ExerciseTierEntity;

@Repository
public interface ExerciseTierRepository extends JpaRepository<ExerciseTierEntity, Long> {
}
