package com.fitcore.api.domain.uesr.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import com.fitcore.api.global.common.entity.BaseTimeEntity;

@Entity
@Table(name = "social_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccountEntity extends BaseTimeEntity {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(36)", nullable = false)
    private UserProfileEntity user;

    @Column(nullable = false)
    private String provider; // 예: "google", "kakao"

    @Column(name = "provider_id", nullable = false, unique = true)
    private String providerId; // 소셜 서비스 고유 식별값

    @Builder
    public SocialAccountEntity(UserProfileEntity user, String provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }
}
