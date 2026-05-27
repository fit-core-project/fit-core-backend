package com.fitcore.api.domain.user.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrengthBaseline {
    private String exerciseId;
    private String exerciseNameSnapshot;
    @DecimalMin(value = "0.0", inclusive = false, message = "workingWeightKg must be greater than 0 kg")
    @DecimalMax(value = "500.0", message = "workingWeightKg must be less than or equal to 500 kg")
    private BigDecimal workingWeightKg;
    @Min(value = 1, message = "reps must be greater than or equal to 1")
    @Max(value = 50, message = "reps must be less than or equal to 50")
    private Integer reps;
}
