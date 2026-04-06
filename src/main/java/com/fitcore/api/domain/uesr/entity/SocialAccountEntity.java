package com.fitcore.api.domain.uesr.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fitcore.api.global.common.entity.BaseEntity;

@Entity
@Table(name = "social_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 안전한 객체 생성 제어
public class SocialAccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 한 명의 유저는 여러 소셜 계정(구글, 카카오 등)을 가질 수 있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String provider; // 예: "google", "kakao"

    @Column(name = "provider_id", nullable = false, unique = true)
    private String providerId; // 소셜 서비스에서 제공하는 고유 식별값 (sub, id 등)

    @Builder
    public SocialAccountEntity(UserEntity user, String provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }
}
