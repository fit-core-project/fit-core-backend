package com.fitcore.api.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.fitcore.api.domain.uesr.entity.UserProfileEntity;
import com.fitcore.api.domain.uesr.repository.UserRepository;
import com.fitcore.api.global.auth.PrincipalDetails;

@Component
public class TokenProvider {
    private final String secret;
    private final long tokenValidityInMilliseconds;
    private Key key; // 변환된 Key 객체 저장

    private final UserRepository userRepository;

    public TokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds, UserRepository userRepository) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
        this.userRepository = userRepository;
    }

    // 1. 주입받은 secret 값을 Base64 Decode해서 Key 변수로 할당
    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 2. 인증 정보를 기반으로 토큰 생성
    public String createToken(Authentication authentication, String profileImageUrl) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
            .setSubject(authentication.getName()) // 보통 email이 들어감
            .claim("auth", authorities)          // 권한 정보 (ROLE_USER 등)
            .claim("profileImage", profileImageUrl)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }

    // 3. 토큰을 받아 인증(Authentication) 객체 반환
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        // 1) 토큰에서 이메일 추출
        String email = claims.getSubject();

        // 2) DB에서 UserProfileEntity 조회
        UserProfileEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // 3) PrincipalDetails 객체 생성 (우리가 만든 객체)
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        // 4) 기존 로직: 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 5) *** 중요: 첫 번째 인자로 String 대신 principalDetails 객체를 삽입!
        return new UsernamePasswordAuthenticationToken(principalDetails, token, authorities);
    }

    // 4. 토큰의 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // 잘못된 서명 또는 형식
        } catch (ExpiredJwtException e) {
            // 만료된 토큰
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 토큰
        } catch (IllegalArgumentException e) {
            // 잘못된 토큰
        }
        return false;
    }
}
