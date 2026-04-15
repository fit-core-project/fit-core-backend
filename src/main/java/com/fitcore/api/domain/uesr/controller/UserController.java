package com.fitcore.api.domain.uesr.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.uesr.response.UserResponse;
import com.fitcore.api.domain.uesr.service.UserService;

@RequestMapping("/api/v1/user")
@RestController
@Tag(name = "User API", description = "유저 관련 API")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "로그인된 유저의 정보를 가져옵니다.")
    @GetMapping("/my/{email}")
    public ResponseEntity<UserResponse> getMyProfile(@PathVariable String email) {
        UserResponse user = userService.getMyProfile(email);

        return ResponseEntity.ok(user);
    }
}
