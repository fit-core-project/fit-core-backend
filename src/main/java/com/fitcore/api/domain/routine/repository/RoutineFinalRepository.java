package com.fitcore.api.domain.routine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;

@Repository
public interface RoutineFinalRepository extends JpaRepository<RoutineFinalEntity, String> {
    // 사용자의 확정된 루틴 페이징 조회
    Page<RoutineFinalEntity> findByUserId(String userId, Pageable pageable);
}