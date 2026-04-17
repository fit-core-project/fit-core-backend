package com.fitcore.api.domain.uesr.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.fitcore.api.domain.uesr.enums.Gender;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {
    private String nickname;
    private Gender gender;
    private LocalDate birthDate;
}
