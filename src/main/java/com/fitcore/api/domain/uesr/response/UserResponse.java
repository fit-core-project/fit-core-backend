package com.fitcore.api.domain.uesr.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fitcore.api.domain.uesr.entity.UserEntity;
import com.fitcore.api.domain.uesr.enums.Gender;
import com.fitcore.api.domain.uesr.enums.UserRole;
import com.fitcore.api.domain.uesr.enums.UserStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private Gender gender;
    private LocalDate birthDate;
    private UserStatus status;
    private Set<UserRole> roles = new HashSet<>();
    private List<String> linkedProviders;

    public UserResponse(UserEntity entity, List<String> linkedProviders) {
        this.email = entity.getEmail();
        this.name = entity.getName();
        this.nickname = entity.getNickname();
        this.profileImageUrl = entity.getProfileImageUrl();
        this.gender = entity.getGender();
        this.birthDate = entity.getBirthDate();
        this.status = entity.getStatus();
        this.roles = entity.getRoles();
        this.linkedProviders = linkedProviders;
    }
}
