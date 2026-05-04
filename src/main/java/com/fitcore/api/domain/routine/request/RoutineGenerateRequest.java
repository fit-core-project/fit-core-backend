package com.fitcore.api.domain.routine.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import com.fitcore.api.domain.routine.dto.Doms;

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
    private List<Doms> doms;
    private List<String> unavailableEquipment;
    private String goal;
    private String userNote;
}
