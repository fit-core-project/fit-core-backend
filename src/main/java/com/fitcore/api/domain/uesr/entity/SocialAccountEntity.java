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

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.fitcore.api.global.common.entity.BaseTimeEntity;

@Entity
@Table(name = "social_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccountEntity extends BaseTimeEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @UuidGenerator // 자동 UUID 생성
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
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
