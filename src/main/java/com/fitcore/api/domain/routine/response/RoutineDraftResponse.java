package com.fitcore.api.domain.routine.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fitcore.api.domain.routine.entity.RoutineDraftEntity;

@Data
@Builder
public class RoutineDraftResponse {
    private String id;
    private String userId;
    private int sourceProfileVersion;
    private Map<String, Object> sourceWorkoutSessionIds;
    private String targetSplitLabel;
    private String generationStatus;
    private String statusReasonCode;
    private boolean isFallback;

    // JSON 데이터들
    private Map<String, Object> requestPayloadSnapshot;
    private Map<String, Object> responsePayloadSnapshot;
    private Map<String, Object> adapterRequestSnapshot;
    private Map<String, Object> adapterResponseSnapshot;
    private List<String> rationaleSummary;

    private LocalDateTime createdAt;

    /**
     * Entity를 Response DTO로 변환하는 정적 메서드
     */
    public static RoutineDraftResponse fromEntity(RoutineDraftEntity entity) {
        return RoutineDraftResponse.builder()
            .id(entity.getId())
            .userId(entity.getUserId())
            .sourceProfileVersion(entity.getSourceProfileVersion())
            .sourceWorkoutSessionIds(entity.getSourceWorkoutSessionIds())
            .targetSplitLabel(entity.getTargetSplitLabel())
            .generationStatus(entity.getGenerationStatus())
            .statusReasonCode(entity.getStatusReasonCode())
            .isFallback(entity.isFallback())
            .requestPayloadSnapshot(entity.getRequestPayloadSnapshot())
            .responsePayloadSnapshot(entity.getResponsePayloadSnapshot())
            .adapterRequestSnapshot(entity.getAdapterRequestSnapshot())
            .adapterResponseSnapshot(entity.getAdapterResponseSnapshot())
            .rationaleSummary(entity.getRationaleSummary())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
