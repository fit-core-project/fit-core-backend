package com.fitcore.api.infrastructure.oauth.userinfo;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo get(String provider, Map<String, Object> attributes) {
        if (provider.equalsIgnoreCase("google")) {
            return new GoogleUserInfo(attributes);
        } else if (provider.equalsIgnoreCase("kakao")) {
            return new KakaoUserInfo(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + provider);
        }
    }
}
