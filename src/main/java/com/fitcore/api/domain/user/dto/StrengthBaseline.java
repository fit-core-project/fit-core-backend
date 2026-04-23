package com.fitcore.api.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrengthBaseline {
    private String exerciseId;
    private String exerciseNameSnapshot;
    private Integer workingWeightKg;
    private Integer reps;
}
