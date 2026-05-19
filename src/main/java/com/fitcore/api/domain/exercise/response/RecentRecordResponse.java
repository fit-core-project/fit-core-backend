package com.fitcore.api.domain.exercise.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor // Builder 사용을 위해 추가
public class RecentRecordResponse {
    private String exerciseId;
    private BigDecimal defaultWeight;
    private int defaultReps;

    // Entity -> Response 변환 정적 메서드
    public static RecentRecordResponse fromEntity(WorkoutSetEntity entity) {
        if (entity == null) {
            return null;
        }

        return RecentRecordResponse.builder()
            .exerciseId(entity.getExerciseId())
            .defaultWeight(entity.getWeightKg() != null ? entity.getWeightKg() : BigDecimal.ZERO)
            .defaultReps(entity.getReps())
            .build();
    }
}
