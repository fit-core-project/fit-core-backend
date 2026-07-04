package com.fitcore.api.domain.program.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.program.entity.RoutineProgramCompletionEventEntity;

public interface RoutineProgramCompletionEventRepository extends JpaRepository<RoutineProgramCompletionEventEntity, String> {
    boolean existsByWorkoutSession_Id(String workoutSessionId);

    boolean existsByProgram_IdAndProgramItem_Id(String programId, String programItemId);

    List<RoutineProgramCompletionEventEntity> findByProgram_Id(String programId);

    void deleteByProgram_Id(String programId);
}
