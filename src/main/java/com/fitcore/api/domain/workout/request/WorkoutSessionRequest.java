package com.fitcore.api.domain.workout.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

import com.fitcore.api.domain.routine.dto.Doms;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class WorkoutSessionRequest {
    @NotNull
    private LocalDate workoutDate;
    private String splitLabel;
    private String sourceRoutineFinalId;
    private String programId;
    private String programItemId;
    private Short timeAvailableMin;
    private Short durationMin;
    private String readinessLevel = "normal"; // 기본값

    // JSON 필드 (프론트에서 Map 형태로 전송)
    private List<String> currentPainAreas;
    private List<Doms> currentDoms;
    private List<String> unavailableEquipment;

    // 세트 정보 리스트
    @NotEmpty
    @Valid
    private List<WorkoutSetRequest> sets;
}
