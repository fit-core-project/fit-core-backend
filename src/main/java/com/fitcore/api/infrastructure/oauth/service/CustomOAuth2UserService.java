package com.fitcore.api.infrastructure.oauth.service;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.user.entity.SocialAccountEntity;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.UserRole;
import com.fitcore.api.domain.user.enums.UserStatus;
import com.fitcore.api.domain.user.repository.SocialAccountRepository;
import com.fitcore.api.domain.user.repository.UserRepository;
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
        String providerId = userInfo.getProviderId(); // 이게 고유 ID입니다.

        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 정보를 불러올 수 없습니다.");
        }

        String clientIp = NetworkUtil.getClientIp(request);
        String userId = (String) request.getSession().getAttribute("link_user_id");
        boolean isLinkMode = "link".equals(request.getSession().getAttribute("oauth_mode"));

        UserProfileEntity user;

        // 1. 소셜 계정 고유 ID로 이미 등록된 계정이 있는지 먼저 확인
        Optional<SocialAccountEntity> socialAccountOpt = socialAccountRepository
            .findByProviderAndProviderId(provider, providerId);

        if (isLinkMode) {
            // [연동 모드]
            user = userRepository.findById(userId)
                .orElseThrow(() -> new OAuth2AuthenticationException("유저를 찾을 수 없습니다."));

            if (socialAccountOpt.isPresent()) {
                if (!socialAccountOpt.get().getUser().getUserId().equals(userId)) {
                    throw new OAuth2AuthenticationException("이미 다른 계정에 연동된 소셜입니다.");
                }
            } else {
                // 연동 정보 저장
                saveSocialAccount(user, provider, providerId, clientIp);
            }
        } else {
            // [일반 로그인 모드]
            if (socialAccountOpt.isPresent()) {
                // 이미 소셜 계정이 존재 -> 기존 유저 가져오기
                user = socialAccountOpt.get().getUser();
                user.setUpdatedIp(clientIp);
            } else {
                // 소셜 계정이 없음 -> 이메일로 기존 유저 확인
                user = userRepository.findByEmail(email)
                    .map(existingUser -> {
                        existingUser.setUpdatedIp(clientIp);
                        return existingUser;
                    })
                    .orElseGet(() -> {
                        // 완전히 신규 유저 생성
                        UserProfileEntity newUser = UserProfileEntity.builder()
                            .email(email)
                            .name(userInfo.getName())
                            .profileImageUrl(userInfo.getImageUrl())
                            .status(UserStatus.ACTIVE)
                            .roles(Collections.singleton(UserRole.ROLE_USER))
                            .build();
                        newUser.setCreatedIp(clientIp);
                        newUser.setUpdatedIp(clientIp);
                        return userRepository.save(newUser);
                    });

                // 찾은 유저(또는 생성된 유저)와 소셜 계정 연결
                saveSocialAccount(user, provider, providerId, clientIp);
            }
        }

        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }

    private void saveSocialAccount(UserProfileEntity user, String provider, String providerId, String ip) {
        SocialAccountEntity newSocial = SocialAccountEntity.builder()
            .provider(provider)
            .providerId(providerId)
            .user(user)
            .build();
        newSocial.setCreatedIp(ip);
        newSocial.setUpdatedIp(ip);
        socialAccountRepository.save(newSocial);
    }
}
