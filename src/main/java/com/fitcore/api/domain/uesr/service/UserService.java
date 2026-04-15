package com.fitcore.api.domain.uesr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.uesr.entity.UserEntity;
import com.fitcore.api.domain.uesr.repository.UserRepository;
import com.fitcore.api.domain.uesr.response.UserResponse;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getMyProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserResponse(user);
    }
}
