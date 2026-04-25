package com.fitcore.api.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyComposition {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate measuredAt;
    private String source;
    private String sourceVendor;
    private Double bodyWeightKg;
    private Double skeletalMuscleMassKg;
    private Double bodyFatMassKg;
    private Double bodyFatPct;
    private Double fatFreeMassKg;
    private Double waistHipRatio;
    private Integer visceralFatLevel;
}
