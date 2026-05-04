package com.fitcore.api.domain.routine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SubstitutionCandidates {
    private String exerciseId;
    private String exerciseName;
    private String reason;
}
