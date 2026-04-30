package com.fitcore.api.infrastructure.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRoutineResponse {

    private String routineDraftId;

    private GenerationStatus generationStatus;

    private StatusReasonCode statusReasonCode;

    @JsonProperty("is_fallback")
    private Boolean isFallback;

    private String summaryTitle;

    private List<String> rationaleSummary;

    private List<RoutineBlockDto> routineBlocks;

    @Builder.Default
    private List<String> warnings = Collections.emptyList();

    // --- Inner DTOs ---

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class RoutineBlockDto {
        private int order;
        private String exerciseId;
        private String exerciseName;
        private String movementPattern;
        private List<String> primaryMuscles;
        private String equipmentType;
        private int defaultRestSec;
        private List<SetPrescriptionDto> prescription;
        private String exerciseRationale;
        private List<SubstitutionCandidateDto> substitutionCandidates;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SetPrescriptionDto {
        private int setIndex;
        private String setType; // default: "working"
        private int targetReps;
        private Double targetWeightKg; // Optional이므로 래퍼 클래스 사용
        private Integer targetRir;     // default: 2
        private int targetRestSec;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SubstitutionCandidateDto {
        private String exerciseId;
        private String exerciseName;
        private String reason;
    }
}