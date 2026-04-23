package com.fitcore.api.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitcore.api.domain.user.entity.SocialAccountEntity;

public interface SocialAccountRepository extends JpaRepository<SocialAccountEntity, String> {
    // 제공자(google, kakao)와 해당 소셜의 고유 ID로 연동 정보 조회
    Optional<SocialAccountEntity> findByProviderAndProviderId(String provider, String providerId);

    List<SocialAccountEntity> findByUserUserId(String userId);
}
