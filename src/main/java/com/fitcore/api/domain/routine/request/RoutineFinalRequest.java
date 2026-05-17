package com.fitcore.api.domain.routine.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fitcore.api.domain.routine.dto.FinalRoutinePayload;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoutineFinalRequest {
    // 어떤 초안을 확정할 것인지 (필수)
    private String routineDraftId;

    // 확정할 운동 날짜
    @NotNull
    private LocalDate targetWorkoutDate;

    @NotNull
    @Valid
    private FinalRoutinePayload finalRoutinePayload;

    // 사용자가 수정 없이 그대로 수락했는지 여부
    @NotNull
    private Boolean acceptedWithoutEdits;

    // 사용자가 수정했다면 그 내용 요약
    private List<String> userEditSummary;
}
