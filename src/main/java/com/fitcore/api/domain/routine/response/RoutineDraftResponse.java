package com.fitcore.api.domain.routine.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.fitcore.api.domain.routine.dto.RoutineBlock;
import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@Data
@Builder
public class RoutineDraftResponse {
    private String routineDraftId;
    private GenerationStatus generationStatus;
    private StatusReasonCode statusReasonCode;
    private boolean isFallback;
    private int totalEstimatedTime;
    private String summaryTitle;
    private List<String> rationaleSummary;
    private List<String> warnings;
    private List<RoutineBlock> routineBlocks;


    public static RoutineDraftResponse fromEntity(RoutineDraftEntity entity) {
        return RoutineDraftResponse.builder()
            .routineDraftId(entity.getId())
            .generationStatus(entity.getGenerationStatus())
            .statusReasonCode(entity.getStatusReasonCode())
            .isFallback(entity.isFallback())
            .totalEstimatedTime(0) // 필드 왜 없음?
            .summaryTitle(entity.getResponsePayloadSnapshot().getSummaryTitle())
            .rationaleSummary(entity.getRationaleSummary())
            .warnings(entity.getResponsePayloadSnapshot().getWarnings())
            .routineBlocks(entity.getResponsePayloadSnapshot().getRoutineBlocks())
            .build();
    }
}
