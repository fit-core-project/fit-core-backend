package com.fitcore.api.domain.user.entity;

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
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.UuidGenerator;

import com.fitcore.api.global.common.entity.BaseTimeEntity;

@Entity
@Table(
    name = "social_accounts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "social_accounts_provider_id_IDX",
            columnNames = {"provider", "provider_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccountEntity extends BaseTimeEntity {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(36)", nullable = false)
    private UserProfileEntity user;

    @Column(nullable = false)
    private String provider;

    // 기존의 unique = true는 제거하고 복합 유니크 키로 관리합니다.
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Builder
    public SocialAccountEntity(UserProfileEntity user, String provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }
}