package com.fitcore.api.domain.workout.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
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
    private String setType = "working";
    private String trackingMode = "weightReps";
    @DecimalMin(value = "0.0", inclusive = false, message = "weightKg must be greater than 0 kg")
    @DecimalMax(value = "500.0", message = "weightKg must be less than or equal to 500 kg")
    private BigDecimal weightKg;
    @NotNull(message = "reps is required")
    @Min(value = 1, message = "reps must be greater than or equal to 1")
    @Max(value = 50, message = "reps must be less than or equal to 50")
    private Integer reps;
    private BigDecimal rpe;
    private BigDecimal rir;
    private Boolean isFailure = false;
    @Min(value = 0, message = "restSec must be greater than or equal to 0 sec")
    @Max(value = 600, message = "restSec must be less than or equal to 600 sec")
    private Integer restSec;
    private String setNote;
}
