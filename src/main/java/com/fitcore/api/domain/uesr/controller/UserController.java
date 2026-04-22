package com.fitcore.api.domain.uesr.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.uesr.request.UserProfileUpdateRequest;
import com.fitcore.api.domain.uesr.response.UserProfileResponse;
import com.fitcore.api.domain.uesr.service.UserService;

@RequestMapping("/api/profile")
@RestController
@Tag(name = "User API", description = "유저 관련 API")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "로그인된 유저의 정보를 가져옵니다.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        UserProfileResponse user = userService.getMyProfile(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "내 프로필 수정", description = "로그인된 유저의 정보를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
        Authentication authentication, @RequestBody UserProfileUpdateRequest userProfileUpdateRequest) {
        String email = (String) authentication.getPrincipal();
        UserProfileResponse user = userService.updateMyProfile(email, userProfileUpdateRequest);
        return ResponseEntity.ok(user);
    }

    // UserController.java
    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("/check-nickname/{nickname}")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@PathVariable String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname);
        return ResponseEntity.ok(isDuplicate); // 중복이면 true, 아니면 false 반환
    }
}
