package com.fitcore.api.domain.workout.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkoutSetRequest {
    private Integer exerciseOrder;
    private String exerciseId;
    private String exerciseNameSnapshot;
    private Integer setIndex;
    private String setType = "working"; // 기본값
    private String trackingMode = "weightReps"; // 기본값
    private BigDecimal weightKg;
    private Integer reps;
    private BigDecimal rpe;
    private BigDecimal rir;
    private Boolean isFailure = false; // 기본값
    private Integer restSec;
    private String setNote;
}
