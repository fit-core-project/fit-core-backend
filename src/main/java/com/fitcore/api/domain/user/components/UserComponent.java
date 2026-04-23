package com.fitcore.api.domain.user.components;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.repository.UserRepository;
import com.fitcore.api.global.common.util.SecurityUtils;

@Component
@RequiredArgsConstructor
public class UserComponent {
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    public Optional<UserProfileEntity> findById() {
        return userRepository.findById(securityUtils.getCurrentUserId());
    }
}
