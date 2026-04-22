package com.fitcore.api.domain.uesr.components;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fitcore.api.domain.uesr.entity.UserProfileEntity;
import com.fitcore.api.domain.uesr.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserComponent {
    private final UserRepository userRepository;

    public Optional<UserProfileEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
