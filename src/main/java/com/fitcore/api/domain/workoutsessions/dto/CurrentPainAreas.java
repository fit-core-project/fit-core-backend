package com.fitcore.api.domain.workoutsessions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 오늘 세션에만 반영될 수 있는 일시 통증
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentPainAreas {
    private String bodyPart;
    private String painLevel;
    private String note;
}
