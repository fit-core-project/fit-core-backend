package com.fitcore.api.domain.workout.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class WorkoutSetRequest {
    @NotNull
    @Min(1)
    private Integer exerciseOrder;
    @NotBlank
    private String exerciseId;
    @NotBlank
    private String exerciseNameSnapshot;
    @NotNull
    @Min(1)
    private Integer setIndex;
    private String setType = "working"; // 기본값
    private String trackingMode = "weightReps"; // 기본값
    private BigDecimal weightKg;
    private Integer reps;
    private BigDecimal rpe;
    private BigDecimal rir;
    private Boolean isFailure = false;
    private Integer restSec;
    private String setNote;
}
