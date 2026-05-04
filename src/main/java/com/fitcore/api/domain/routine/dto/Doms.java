package com.fitcore.api.domain.routine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doms {
    @NotNull(message = "부위는 필수입니다.")
    private String bodyPart;
    @NotNull(message = "통증 레벨은 필수입니다.")
    private String level;
}
