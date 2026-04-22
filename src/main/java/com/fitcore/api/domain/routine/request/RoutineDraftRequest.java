package com.fitcore.api.domain.routine.request;

import lombok.Data;

import java.util.Map;

@Data
public class RoutineDraftRequest {

    // 1. 루틴 생성의 기준이 되는 프로파일 버전
    private int sourceProfileVersion;

    // 2. 루틴 생성의 재료가 되는 운동 세션 ID들 (JSON 구조)
    // 엔티티와 동일한 타입(Map)으로 구성하여 직렬화/역직렬화 효율을 높였습니다.
    private Map<String, Object> sourceWorkoutSessionIds;

    // 3. 목표로 하는 분할 설정 (예: "Upper", "Lower", "Push", "Pull" 등)
    private String targetSplitLabel;

    // 4. 루틴 생성 요청 시점의 상세 설정값
    // 사용자가 입력한 추가적인 요구사항이나 옵션 등을 담는 JSON
    private Map<String, Object> requestPayloadSnapshot;
}
