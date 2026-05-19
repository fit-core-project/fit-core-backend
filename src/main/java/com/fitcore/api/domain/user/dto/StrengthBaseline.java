package com.fitcore.api.domain.user.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrengthBaseline {
    private String exerciseId;
    private String exerciseNameSnapshot;
    private BigDecimal workingWeightKg;
    private Integer reps;
}
