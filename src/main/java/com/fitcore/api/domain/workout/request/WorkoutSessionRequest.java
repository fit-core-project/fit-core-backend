package com.fitcore.api.domain.workout.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fitcore.api.domain.workout.dto.CurrentPainAreas;
import com.fitcore.api.domain.workout.dto.Doms;

@Data
public class WorkoutSessionRequest {
    private LocalDate workoutDate;
    private String splitLabel;
    private String sourceRoutineFinalId;
    private Short timeAvailableMin;
    private Short durationMin;
    private String readinessLevel = "normal"; // 기본값

    // JSON 필드 (프론트에서 Map 형태로 전송)
    private List<CurrentPainAreas> currentPainAreas;
    private List<Doms> doms;
    private Map<String, Object> unavailableEquipment;

    private String sessionNote;

    // 세트 정보 리스트
    private List<WorkoutSetRequest> sets;
}
