package com.fitcore.api.infrastructure.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private List<Map<String, String>> painAreas = Collections.emptyList();
    private Map<String, Integer> domsData = Collections.emptyMap();
    private List<String> equipment = Collections.emptyList();
    private String goal;
    private String userNote;
}
