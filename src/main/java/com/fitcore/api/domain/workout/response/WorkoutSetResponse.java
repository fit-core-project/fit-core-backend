package com.fitcore.api.domain.workout.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;

@Data
@Builder
public class WorkoutSetResponse {
    private String id;
    private Integer exerciseOrder;
    private String exerciseId;
    private String exerciseNameSnapshot;
    private Integer setIndex;
    private String setType;
    private String trackingMode;
    private BigDecimal weightKg;
    private Integer reps;
    private BigDecimal rpe;
    private BigDecimal rir;
    private Boolean isFailure;
    private Integer restSec;
    private String setNote;

    public static WorkoutSetResponse fromEntity(WorkoutSetEntity entity) {
        return WorkoutSetResponse.builder()
            .id(entity.getId())
            .exerciseOrder(entity.getExerciseOrder())
            .exerciseId(entity.getExerciseId())
            .exerciseNameSnapshot(entity.getExerciseNameSnapshot())
            .setIndex(entity.getSetIndex())
            .setType(entity.getSetType())
            .trackingMode(entity.getTrackingMode())
            .weightKg(entity.getWeightKg())
            .reps(entity.getReps())
            .rpe(entity.getRpe())
            .rir(entity.getRir())
            .isFailure(entity.getIsFailure())
            .restSec(entity.getRestSec())
            .setNote(entity.getSetNote())
            .build();
    }
}
