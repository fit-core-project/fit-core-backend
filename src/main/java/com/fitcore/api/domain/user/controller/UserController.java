package com.fitcore.api.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.user.request.UserProfileUpdateRequest;
import com.fitcore.api.domain.user.response.UserConditionResponse;
import com.fitcore.api.domain.user.response.UserPreferencesResponse;
import com.fitcore.api.domain.user.response.UserProfileResponse;
import com.fitcore.api.domain.user.service.UserService;

@RequestMapping("/api")
@RestController
@Tag(name = "User Profile API", description = "유저 프로필 API")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "로그인된 유저의 정보를 가져옵니다.")
    @GetMapping("/profile/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        UserProfileResponse user = userService.getMyProfile();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "내 컨디션 조회", description = "로그인된 유저의 컨디션 정보를 가져옵니다.")
    @GetMapping("/users/condition")
    public ResponseEntity<UserConditionResponse> getUserCondition() {
        return ResponseEntity.ok(userService.getUserCondition());
    }

    @Operation(summary = "내 컨디션 조회", description = "로그인된 유저의 사전 운동 설정을 가져옵니다.")
    @GetMapping("/users/preferences")
    public ResponseEntity<UserPreferencesResponse> getUserPreferences() {
        return ResponseEntity.ok(userService.getUserPreferences());
    }
    
    @Operation(summary = "내 프로필 수정", description = "로그인된 유저의 정보를 수정합니다.")
    @PutMapping("/profile/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
        @RequestBody UserProfileUpdateRequest userProfileUpdateRequest) {
        UserProfileResponse user = userService.updateMyProfile(userProfileUpdateRequest);
        return ResponseEntity.ok(user);
    }

    // UserController.java
    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("/profile/check-nickname/{nickname}")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@PathVariable String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname);
        return ResponseEntity.ok(isDuplicate); // 중복이면 true, 아니면 false 반환
    }
}
