package com.fitcore.api.domain.program.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.program.entity.RoutineProgramEntity;
import com.fitcore.api.domain.program.entity.RoutineProgramStatus;

public interface RoutineProgramRepository extends JpaRepository<RoutineProgramEntity, String> {
    boolean existsByUserIdAndStatus(String userId, RoutineProgramStatus status);

    Optional<RoutineProgramEntity> findByUserIdAndStatus(String userId, RoutineProgramStatus status);

    List<RoutineProgramEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
