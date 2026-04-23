package com.fitcore.api.domain.workoutsessions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 지연성 근육통/피로 정보
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doms {
    private String bodyPart;
    private String level;
}
