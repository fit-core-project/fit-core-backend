package com.fitcore.api.infrastructure.oauth.service;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.get(provider, oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 정보를 불러올 수 없습니다.");
        }

        String clientIp = NetworkUtil.getClientIp(request);

        // 1. 회원 정보 처리
        UserEntity user = userRepository.findByEmail(email)
            .map(existingUser -> {
                existingUser.setUpdateIp(clientIp);
                // 필요하다면 프로필 이미지나 이름 업데이트 로직 추가 가능
                return existingUser;
            })
            .orElseGet(() -> {
                UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .name(userInfo.getName())
                    .profileImageUrl(userInfo.getImageUrl()) // getImageUrl -> getProfileImageUrl 확인
                    .status(UserStatus.ACTIVE)
                    .roles(Collections.singleton(UserRole.ROLE_USER))
                    .build();
                newUser.setInsertIp(clientIp);
                newUser.setUpdateIp(clientIp);
                return userRepository.save(newUser);
            });

        // 2. 소셜 연동 정보 처리
        socialAccountRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
            .ifPresentOrElse(
                social -> social.setUpdateIp(clientIp),
                () -> {
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

        // PrincipalDetails 생성 시 attributes는 원본(oAuth2User.getAttributes())을 넘겨주는 것이 관례입니다.
        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }
}
