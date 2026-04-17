package com.fitcore.api.domain.uesr.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.uesr.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 계정 통합의 기준이 되는 이메일 조회
    Optional<UserEntity> findByEmail(String email);

    boolean existsByNickname(String nickname);
}
