package com.fitcore.api.domain.program.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.program.entity.RoutineProgramItemEntity;

public interface RoutineProgramItemRepository extends JpaRepository<RoutineProgramItemEntity, String> {
    List<RoutineProgramItemEntity> findByProgram_IdOrderByPositionAsc(String programId);

    Optional<RoutineProgramItemEntity> findByProgram_IdAndPosition(String programId, int position);

    long countByProgram_Id(String programId);
}
