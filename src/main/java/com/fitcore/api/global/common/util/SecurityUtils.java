package com.fitcore.api.global.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fitcore.api.global.auth.PrincipalDetails;

@Component
public class SecurityUtils {
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Authentication is missing.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof PrincipalDetails details) {
            return details.getUser().getUserId();
        }
        if (principal instanceof String userId && !"anonymousUser".equals(userId)) {
            return userId;
        }

        throw new IllegalStateException("Unsupported authentication principal.");
    }
}
