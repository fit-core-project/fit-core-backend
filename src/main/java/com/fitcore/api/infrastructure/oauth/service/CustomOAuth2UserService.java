package com.fitcore.api.infrastructure.oauth.service;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.fitcore.api.domain.uesr.entity.SocialAccountEntity;
import com.fitcore.api.domain.uesr.entity.UserEntity;
import com.fitcore.api.domain.uesr.enums.UserRole;
import com.fitcore.api.domain.uesr.enums.UserStatus;
import com.fitcore.api.domain.uesr.repository.SocialAccountRepository;
import com.fitcore.api.domain.uesr.repository.UserRepository;
import com.fitcore.api.global.auth.PrincipalDetails;
import com.fitcore.api.global.common.util.NetworkUtil;
import com.fitcore.api.infrastructure.oauth.userinfo.OAuth2UserInfo;
import com.fitcore.api.infrastructure.oauth.userinfo.OAuth2UserInfoFactory;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final HttpServletRequest request;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.get(provider, oAuth2User.getAttributes());

        // 현재 접속 IP 추출
        String clientIp = NetworkUtil.getClientIp(request);

        // 1. 회원 정보 처리 (가입 또는 정보 업데이트)
        UserEntity user = userRepository.findByEmail(userInfo.getEmail())
            .map(existingUser -> {
                // 기존 회원이면 마지막 수정 IP 갱신
                existingUser.setUpdateIp(clientIp);
                return existingUser;
            })
            .orElseGet(() -> {
                // 신규 회원이면 빌더로 생성 (IP 포함)
                UserEntity newUser = UserEntity.builder()
                    .email(userInfo.getEmail())
                    .name(userInfo.getName())
                    .profileImageUrl(userInfo.getImageUrl())
                    .status(UserStatus.ACTIVE)
                    .roles(Collections.singleton(UserRole.ROLE_USER)) // Enum 적용
                    .build();
                newUser.setInsertIp(clientIp); // 등록 IP 세팅
                newUser.setUpdateIp(clientIp);
                return userRepository.save(newUser);
            });

        // 2. 소셜 연동 정보 처리
        socialAccountRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
            .ifPresentOrElse(
                social -> social.setUpdateIp(clientIp), // 이미 연동됨 -> 수정 IP 갱신
                () -> {
                    // 미연동 -> 새 연동 정보 저장
                    SocialAccountEntity newSocial = SocialAccountEntity.builder()
                        .provider(provider)
                        .providerId(userInfo.getProviderId())
                        .user(user)
                        .build();
                    newSocial.setInsertIp(clientIp);
                    newSocial.setUpdateIp(clientIp);
                    socialAccountRepository.save(newSocial);
                }
            );

        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }
}
