package com.fitcore.api.domain.routine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;

@Repository
public interface RoutineDraftRepository extends JpaRepository<RoutineDraftEntity, String> {
    // 필요한 경우 사용자별 draft 조회 등을 추가
}
