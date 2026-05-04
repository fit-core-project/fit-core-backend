package com.fitcore.api.domain.routine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RoutineBlock {
    private int order;
    private String exerciseId;
    private String exerciseName;
    private String movementPattern;
    private List<String> primaryMuscles;
    private String equipmentType;
    private int defaultRestSec;
    private List<Prescription> prescription;
    private String exerciseRationale;
    private List<SubstitutionCandidates> substitutionCandidates;
}
