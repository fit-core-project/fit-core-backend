package com.fitcore.api.domain.workout.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AttendanceWeekResponse {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private int actualDays;
    private Integer targetDays;
    private Double rate;
}
