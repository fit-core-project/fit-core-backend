package com.fitcore.api.global.auth;

import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.fitcore.api.domain.uesr.entity.UserProfileEntity;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final UserProfileEntity user; // 우리가 만든 엔티티
    private Map<String, Object> attributes; // 소셜에서 받은 속성값

    // 일반 로그인용 생성자
    public PrincipalDetails(UserProfileEntity user) {
        this.user = user;
    }

    // 소셜 로그인용 생성자
    public PrincipalDetails(UserProfileEntity user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    /**
     * OAuth2User 인터페이스 구현
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    /**
     * UserDetails 인터페이스 구현 (권한 설정)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.name())) // Enum의 이름(예: "ROLE_USER")을 String으로 추출
            .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인은 비밀번호가 필요 없음
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 계정 상태 관리 (모두 true로 설정해야 로그인이 가능함)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
