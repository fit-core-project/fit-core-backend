package com.fitcore.api.domain.exercise.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseTierRequest {
    @NotBlank
    private String nameKr;
    @NotBlank
    private String nameEn;
    @NotBlank
    private String primaryMuscle;
    private String secondaryMuscle;
    @NotBlank
    private String equipmentReq;
    @NotNull
    private Long difficultyTier;
    @NotNull
    private Long efficiencyTier;
    @NotBlank
    private String painTriggers;
    @NotBlank
    private String movementType;
    private String substituteExerciseIds;
}
