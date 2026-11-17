package com.fitcore.api.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.user.entity.SocialAccountEntity;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.repository.SocialAccountRepository;
import com.fitcore.api.domain.user.repository.UserRepository;
import com.fitcore.api.domain.user.request.UserProfileUpdateRequest;
import com.fitcore.api.domain.user.response.UserConditionResponse;
import com.fitcore.api.domain.user.response.UserPreferencesResponse;
import com.fitcore.api.domain.user.response.UserProfileResponse;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    public UserProfileResponse getMyProfile() {
        String userId = securityUtils.getCurrentUserId();
        UserProfileEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<String> linkedProviders = socialAccountRepository.findByUserUserId(user.getUserId())
            .stream()
            .map(SocialAccountEntity::getProvider)
            .toList();

        return new UserProfileResponse(user, linkedProviders);
    }

    public UserConditionResponse getUserCondition() {
        String userId = securityUtils.getCurrentUserId();
        UserProfileEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserConditionResponse(user);
    }

    public UserPreferencesResponse getUserPreferences() {
        String userId = securityUtils.getCurrentUserId();
        UserProfileEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserPreferencesResponse(user);
    }

    public UserProfileResponse updateMyProfile(UserProfileUpdateRequest request) {
        String userId = securityUtils.getCurrentUserId();
        UserProfileEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(request);

        return getMyProfile();
    }

    /**
     * 회원 탈퇴: 개인 정보를 비우고 소셜 연동을 끊은 뒤 소프트 삭제한다.
     */
    public void withdrawMe(String clientIp) {
        String userId = securityUtils.getCurrentUserId();
        UserProfileEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        socialAccountRepository.deleteAll(socialAccountRepository.findByUserUserId(userId));
        user.withdraw(clientIp);
        // @SoftDelete: delete()가 is_deleted를 세운다. 익명화한 값이 먼저 반영되도록 flush 후 삭제한다.
        userRepository.saveAndFlush(user);
        userRepository.delete(user);
    }

    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
