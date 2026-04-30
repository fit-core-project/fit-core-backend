package com.fitcore.api.infrastructure.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class AiExercisePlan {

    @JsonProperty("exercise_name")
    private String exerciseName;

    @JsonProperty("target_weight")
    private Integer targetWeight; // null 허용을 위해 Integer 사용

    @JsonProperty("reps")
    private int reps;

    @JsonProperty("sets")
    private int sets;

    @JsonProperty("rest_time_sec")
    private int restTimeSec;

    @JsonProperty("coach_tip")
    private String coachTip;
}
