package com.fitcore.api.infrastructure.oauth.controller;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.uesr.components.UserComponent;
import com.fitcore.api.domain.uesr.entity.UserProfileEntity;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserComponent userComponent;

    @PostMapping("/api/v1/auth/set-link-mode")
    public void setLinkMode(Authentication authentication, HttpServletRequest request) {
        // Principal이 String 타입(이메일)임을 알고 있으므로 바로 꺼냅니다.
        String email = (String) authentication.getPrincipal();
        // 이메일로 유저를 찾아 필요한 로직 수행
        UserProfileEntity user = userComponent.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 이제 세션 저장 로직 진행
        request.getSession().setAttribute("link_user_id", user.getUserId());
        request.getSession().setAttribute("oauth_mode", "link");
    }
}
