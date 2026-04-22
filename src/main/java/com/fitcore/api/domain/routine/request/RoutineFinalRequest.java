package com.fitcore.api.domain.routine.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class RoutineFinalRequest {
    // 어떤 초안을 확정할 것인지 (필수)
    private String routineDraftId;

    // 확정할 운동 날짜
    private LocalDate targetWorkoutDate;

    // 확정 시 목표 부위 (초안과 다를 경우 덮어쓰기 가능하도록)
    private String targetSplitLabel;

    // 사용자가 수정 없이 그대로 수락했는지 여부
    private Boolean acceptedWithoutEdits;

    // 사용자가 수정했다면 그 내용 요약
    private Map<String, Object> userEditSummary;
}