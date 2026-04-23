package com.fitcore.api.domain.workoutsessions.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fitcore.api.domain.workoutsessions.dto.CurrentPainAreas;
import com.fitcore.api.domain.workoutsessions.dto.Doms;
import com.fitcore.api.domain.workoutsessions.entity.WorkoutSessionEntity;

@Data
@Builder
public class WorkoutSessionResponse {
    private String id;
    private String userId;
    private LocalDate workoutDate;
    private String splitLabel;
    private String sourceRoutineFinalId;
    private Short timeAvailableMin;
    private Short durationMin;
    private String readinessLevel;

    // JSON 필드
    private List<CurrentPainAreas> currentPainAreas;
    private List<Doms> doms;
    private Map<String, Object> unavailableEquipment;

    private String sessionNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 연관된 세트 리스트
    private List<WorkoutSetResponse> sets;

    public static WorkoutSessionResponse fromEntity(WorkoutSessionEntity entity) {
        return WorkoutSessionResponse.builder()
            .id(entity.getId())
            .userId(entity.getUserId())
            .workoutDate(entity.getWorkoutDate())
            .splitLabel(entity.getSplitLabel())
            .sourceRoutineFinalId(entity.getSourceRoutineFinalId())
            .timeAvailableMin(entity.getTimeAvailableMin())
            .durationMin(entity.getDurationMin())
            .readinessLevel(entity.getReadinessLevel())
            .currentPainAreas(entity.getCurrentPainAreas())
            .doms(entity.getDoms())
            .unavailableEquipment(entity.getUnavailableEquipment())
            .sessionNote(entity.getSessionNote())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .sets(entity.getWorkoutSets().stream()
                .map(WorkoutSetResponse::fromEntity)
                .collect(Collectors.toList()))
            .build();
    }
}