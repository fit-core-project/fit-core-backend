package com.fitcore.api.domain.routine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doms {
    @NotBlank(message = "currentDoms bodyPart is required")
    private String bodyPart;

    @NotBlank(message = "currentDoms level is required")
    @Pattern(regexp = "(?i)mild|moderate|severe", message = "currentDoms level must be mild, moderate, or severe")
    private String level;

    private LocalDate recordedAt;
}
