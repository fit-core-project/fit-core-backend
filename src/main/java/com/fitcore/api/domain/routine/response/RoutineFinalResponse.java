package com.fitcore.api.domain.routine.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fitcore.api.domain.routine.dto.FinalRoutinePayload;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;

@Data
@Builder
public class RoutineFinalResponse {
    private String routineFinalId;
    private String routineDraftId;
    private String userId;
    private LocalDate targetWorkoutDate;
    private String targetSplitLabel;

    private FinalRoutinePayload finalRoutinePayload;

    private Boolean acceptedWithoutEdits;
    private List<String> userEditSummary;
    private LocalDateTime savedAt;

    public static RoutineFinalResponse fromEntity(RoutineFinalEntity entity) {
        return RoutineFinalResponse.builder()
            .routineFinalId(entity.getId())
            .routineDraftId(entity.getRoutineDraft().getId())
            .userId(entity.getUserId())
            .targetWorkoutDate(entity.getTargetWorkoutDate())
            .targetSplitLabel(entity.getTargetSplitLabel())
            .finalRoutinePayload(entity.getFinalRoutinePayload())
            .acceptedWithoutEdits(entity.getAcceptedWithoutEdits())
            .userEditSummary(entity.getUserEditSummary())
            .savedAt(entity.getSavedAt())
            .build();
    }
}
