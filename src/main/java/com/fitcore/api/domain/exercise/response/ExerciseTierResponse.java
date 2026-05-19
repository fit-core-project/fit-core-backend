package com.fitcore.api.domain.exercise.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fitcore.api.domain.exercise.entity.ExerciseTierEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseTierResponse {
    private Long id;
    private String nameKr;
    private String nameEn;
    private String primaryMuscle;
    private List<String> secondaryMuscle;
    private String equipment;
    private Long tier;

    public static ExerciseTierResponse fromEntity(ExerciseTierEntity entity) {
        return ExerciseTierResponse.builder()
            .id(entity.getId())
            .nameKr(entity.getNameKr())
            .nameEn(entity.getNameEn())
            .primaryMuscle(entity.getPrimaryMuscle())
            .secondaryMuscle(
                Optional.ofNullable(entity.getSecondaryMuscle())
                    .filter(s -> !s.isBlank()) // 빈 문자열인지 체크
                    .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)      // 앞뒤 공백 제거
                        .filter(str -> !str.isEmpty()) // 공백만 있던 항목 제거
                        .toList())
                    .orElse(Collections.emptyList()) // null이거나 빈 문자열이면 빈 리스트 반환
            )
            .equipment(entity.getEquipmentReq())
            .tier(entity.getDifficultyTier())
            .build();
    }
}
