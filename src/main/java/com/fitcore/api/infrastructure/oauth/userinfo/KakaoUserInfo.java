package com.fitcore.api.infrastructure.oauth.userinfo;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        return account != null ? (String) account.get("email") : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getName() {
        // 카카오는 실명을 제공하지 않는 경우가 많아 닉네임을 이름 대용으로 주로 사용합니다.
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties != null ? (String) properties.get("nickname") : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getImageUrl() {
        // 프로필 이미지는 properties 혹은 kakao_account.profile 내에 존재합니다.
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties != null ? (String) properties.get("profile_image") : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getNickname() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties != null ? (String) properties.get("nickname") : null;
    }
}
