package com.fitcore.api.domain.workout.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;


@Getter
@Setter
@ToString
@NoArgsConstructor
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
    private Boolean isFailure = false;
    private Integer restSec;
    private String setNote;
}
