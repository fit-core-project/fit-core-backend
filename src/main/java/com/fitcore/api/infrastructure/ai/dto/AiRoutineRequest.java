package com.fitcore.api.infrastructure.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AiRoutineRequest {
    private String userId;
    private String targetSplitLabel; // "push", "pull", "legs" 등
    private List<String> targetMuscles = Collections.emptyList();
    private String readinessLevel = "normal";
    private int timeAvailableMin;
    private List<String> currentPainAreas = Collections.emptyList();
    private List<DomEntryDto> doms = Collections.emptyList();
    private List<String> unavailableEquipment = Collections.emptyList();
    private String goal;
    private String userNote;

    // 파이썬의 DomEntry 클래스에 대응하는 내부 DTO 또는 별도 클래스
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomEntryDto {
        private String bodyPart;
        private String level;
    }
}