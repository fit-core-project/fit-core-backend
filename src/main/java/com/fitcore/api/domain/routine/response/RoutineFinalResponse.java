package com.fitcore.api.domain.routine.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;

@Data
@Builder
public class RoutineFinalResponse {
    private String id;
    private String routineDraftId;
    private String userId;
    private LocalDate targetWorkoutDate;
    private String targetSplitLabel;

    // 확정된 루틴의 상세 데이터 (JSON)
    private Map<String, Object> finalRoutinePayload;

    private Boolean acceptedWithoutEdits;
    private Map<String, Object> userEditSummary;
    private LocalDateTime savedAt;

    /**
     * Entity를 Response DTO로 변환하는 정적 메서드
     */
    public static RoutineFinalResponse fromEntity(RoutineFinalEntity entity) {
        return RoutineFinalResponse.builder()
            .id(entity.getId())
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
