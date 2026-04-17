package com.fitcore.api.domain.uesr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.uesr.entity.SocialAccountEntity;
import com.fitcore.api.domain.uesr.entity.UserEntity;
import com.fitcore.api.domain.uesr.repository.SocialAccountRepository;
import com.fitcore.api.domain.uesr.repository.UserRepository;
import com.fitcore.api.domain.uesr.request.UserUpdateRequest;
import com.fitcore.api.domain.uesr.response.UserResponse;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    public UserResponse getMyProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<String> linkedProviders = socialAccountRepository.findByUserUserId(user.getUserId())
            .stream()
            .map(SocialAccountEntity::getProvider)
            .toList();

        return new UserResponse(user, linkedProviders);
    }

    public UserResponse updateMyProfile(String email, UserUpdateRequest request) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(request.getNickname(), request.getGender(), request.getBirthDate());

        return getMyProfile(email);
    }

    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
