package com.fitcore.api.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

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
    @DecimalMin(value = "20.0", message = "bodyWeightKg must be greater than or equal to 20 kg")
    @DecimalMax(value = "300.0", message = "bodyWeightKg must be less than or equal to 300 kg")
    private Double bodyWeightKg;
    @DecimalMin(value = "5.0", message = "skeletalMuscleMassKg must be greater than or equal to 5 kg")
    @DecimalMax(value = "100.0", message = "skeletalMuscleMassKg must be less than or equal to 100 kg")
    private Double skeletalMuscleMassKg;
    private Double bodyFatMassKg;
    @DecimalMin(value = "1.0", message = "bodyFatPct must be greater than or equal to 1%")
    @DecimalMax(value = "60.0", message = "bodyFatPct must be less than or equal to 60%")
    private Double bodyFatPct;
    private Double fatFreeMassKg;
    private Double waistHipRatio;
    private Integer visceralFatLevel;
}
