package com.fitcore.api.domain.uesr.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.uesr.entity.UserProfileEntity;

public interface UserRepository extends JpaRepository<UserProfileEntity, UUID> {
    // 계정 통합의 기준이 되는 이메일 조회
    Optional<UserProfileEntity> findByEmail(String email);

    boolean existsByNickname(String nickname);
}
