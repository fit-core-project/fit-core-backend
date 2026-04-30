package com.fitcore.api.domain.routine.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoutineGenerateRequest {
    private String targetSplitLabel;
    private List<String> targetMuscles;
    private String readinessLevel;
    private int timeAvailableMin;
    private List<String> currentPainAreas;
    private List<DomsRequest> doms;
    private List<String> unavailableEquipment;
    private String goal;
    private String userNote;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomsRequest {
        @NotNull(message = "부위는 필수입니다.")
        private String bodyPart;
        @NotNull(message = "통증 레벨은 필수입니다.")
        private String level;
    }
}
