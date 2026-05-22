package com.fitcore.api.domain.workout.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PrResponse {
    private String exerciseId;
    private String exerciseNameSnapshot;
    private BigDecimal estimated1RM;
    private BigDecimal weightKg;
    private Integer reps;
    private LocalDate achievedDate;
}
