package com.fitcore.api.infrastructure.oauth.userinfo;

public interface OAuth2UserInfo {
    String getProviderId();    // 소셜 고유 식별자 (sub, id 등)

    String getProvider();      // google, kakao 등

    String getEmail();

    String getName();

    // 추가된 메서드들
    String getImageUrl();      // 프로필 이미지 경로

    String getNickname();      // 닉네임 (제공 시)
}
