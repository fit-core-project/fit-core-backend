package com.fitcore.api.domain.routine.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import com.fitcore.api.domain.routine.dto.Doms;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoutineGenerateRequest {
    private String targetSplitLabel;

    @NotNull(message = "targetMuscles is required")
    @NotEmpty(message = "targetMuscles must not be empty")
    @Size(max = 20, message = "targetMuscles must contain at most 20 items")
    private List<@NotBlank(message = "targetMuscles item must not be blank") String> targetMuscles;

    @NotBlank(message = "readinessLevel is required")
    @Pattern(regexp = "(?i)low|normal|high", message = "readinessLevel must be low, normal, or high")
    private String readinessLevel;

    @NotNull(message = "timeAvailableMin is required")
    @Min(value = 10, message = "timeAvailableMin must be at least 10")
    @Max(value = 180, message = "timeAvailableMin must be at most 180")
    private Integer timeAvailableMin;

    @Size(max = 30, message = "currentPainAreas must contain at most 30 items")
    private List<@NotBlank(message = "currentPainAreas item must not be blank") String> currentPainAreas;

    @Valid
    @Size(max = 30, message = "currentDoms must contain at most 30 items")
    private List<@Valid Doms> currentDoms;

    @Size(max = 50, message = "unavailableEquipment must contain at most 50 items")
    private List<@NotBlank(message = "unavailableEquipment item must not be blank") String> unavailableEquipment;

    @NotBlank(message = "goal is required")
    @Size(max = 64, message = "goal must be at most 64 characters")
    private String goal;

    @Size(max = 1000, message = "userNote must be at most 1000 characters")
    private String userNote;
}
