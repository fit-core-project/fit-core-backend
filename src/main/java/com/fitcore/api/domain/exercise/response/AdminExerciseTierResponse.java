package com.fitcore.api.domain.exercise.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fitcore.api.domain.exercise.entity.ExerciseTierEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminExerciseTierResponse {
    private Long id;
    private String nameKr;
    private String nameEn;
    private String primaryMuscle;
    private String secondaryMuscle;
    private String equipmentReq;
    private Long difficultyTier;
    private Long efficiencyTier;
    private String painTriggers;
    private String movementType;
    private String substituteExerciseIds;

    public static AdminExerciseTierResponse fromEntity(ExerciseTierEntity e) {
        return AdminExerciseTierResponse.builder()
            .id(e.getId())
            .nameKr(e.getNameKr())
            .nameEn(e.getNameEn())
            .primaryMuscle(e.getPrimaryMuscle())
            .secondaryMuscle(e.getSecondaryMuscle())
            .equipmentReq(e.getEquipmentReq())
            .difficultyTier(e.getDifficultyTier())
            .efficiencyTier(e.getEfficiencyTier())
            .painTriggers(e.getPainTriggers())
            .movementType(e.getMovementType())
            .substituteExerciseIds(e.getSubstituteExerciseIds())
            .build();
    }
}
