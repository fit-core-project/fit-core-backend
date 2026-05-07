package com.fitcore.api.domain.exercise.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.fitcore.api.domain.workout.entity.WorkoutSetEntity;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor // Builder 사용을 위해 추가
public class RecentRecordResponse {
    private String exerciseId;
    private int defaultWeight;
    private int defaultReps;

    // Entity -> Response 변환 정적 메서드
    public static RecentRecordResponse fromEntity(WorkoutSetEntity entity) {
        if (entity == null) {
            return null;
        }

        return RecentRecordResponse.builder()
            .exerciseId(entity.getExerciseId())
            // BigDecimal을 int로 변환 (소수점 버림)
            .defaultWeight(entity.getWeightKg() != null ? entity.getWeightKg().intValue() : 0)
            .defaultReps(entity.getReps())
            .build();
    }
}