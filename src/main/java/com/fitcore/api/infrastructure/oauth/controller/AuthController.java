package com.fitcore.api.infrastructure.oauth.controller;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.uesr.components.UserComponent;
import com.fitcore.api.domain.uesr.entity.UserProfileEntity;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserComponent userComponent;

    @PostMapping("/api/v1/auth/set-link-mode")
    public void setLinkMode(HttpServletRequest request) {
        UserProfileEntity user = userComponent.findById()
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 이제 세션 저장 로직 진행
        request.getSession().setAttribute("link_user_id", user.getUserId());
        request.getSession().setAttribute("oauth_mode", "link");
    }
}
