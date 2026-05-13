package com.fitcore.api.domain.routine.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.fitcore.api.domain.routine.dto.RoutineBlock;
import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@Data
@Builder
public class RoutineDraftResponse {
    private String routineDraftId;
    private GenerationStatus generationStatus;
    private StatusReasonCode statusReasonCode;
    @JsonProperty("isFallback")
    private boolean isFallback;
    private int totalEstimatedTime;
    private String summaryTitle;
    private List<String> rationaleSummary;
    private List<String> warnings;
    private List<RoutineBlock> routineBlocks;

    public static RoutineDraftResponse fromEntity(RoutineDraftEntity entity) {
        AiRoutineResponse res = entity.getResponsePayloadSnapshot();
        return RoutineDraftResponse.builder()
            .routineDraftId(entity.getId())
            .generationStatus(entity.getGenerationStatus())
            .statusReasonCode(entity.getStatusReasonCode())
            .isFallback(entity.isFallback())
            .totalEstimatedTime(resolveTotalEstimatedTime(res))
            .summaryTitle(res.getSummaryTitle())
            .rationaleSummary(entity.getRationaleSummary())
            .warnings(res.getWarnings())
            .routineBlocks(res.getRoutineBlocks())
            .build();
    }

    private static int resolveTotalEstimatedTime(AiRoutineResponse res) {
        if (res.getTotalEstimatedTime() != null && res.getTotalEstimatedTime() > 0) {
            return res.getTotalEstimatedTime();
        }
        if (res.getRoutineBlocks() == null || res.getRoutineBlocks().isEmpty()) {
            return 0;
        }
        int totalSec = res.getRoutineBlocks().stream()
            .flatMap(b -> b.getPrescription().stream())
            .mapToInt(p -> 45 + (p.getTargetRestSec() > 0 ? p.getTargetRestSec() : 90))
            .sum();
        return Math.max(1, totalSec / 60);
    }
}
