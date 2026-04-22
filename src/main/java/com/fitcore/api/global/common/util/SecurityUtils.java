package com.fitcore.api.global.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fitcore.api.global.auth.PrincipalDetails;

@Component
public class SecurityUtils {
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않았거나 익명 사용자인 경우 예외 처리
        if (authentication == null || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        // 인증 객체에서 PrincipalDetails 추출
        Object principal = authentication.getPrincipal();


        System.out.println(principal.getClass());
        System.out.println(principal.toString());

        if (principal instanceof PrincipalDetails details) {
            System.out.println(details.getUser().getUserId());
            return details.getUser().getUserId();
        }
        System.out.println(
            "AAAAAAAAAAAAAAAA"
        );
        throw new IllegalStateException("인증 객체에서 유저 정보를 찾을 수 없습니다.");
    }
}
