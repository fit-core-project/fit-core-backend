package com.fitcore.api.domain.nutrition.response;

import com.fitcore.api.domain.nutrition.entity.NutritionTargetEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class NutritionTargetResponse {

    private Integer kcalGoal;
    private BigDecimal proteinGMin;
    private BigDecimal proteinGMax;
    private BigDecimal carbsGMin;
    private BigDecimal carbsGMax;
    private BigDecimal fatGMin;
    private BigDecimal fatGMax;
    private BigDecimal sugarMax;
    private BigDecimal fiberMin;
    private Integer sodiumMax;

    public static NutritionTargetResponse fromEntity(NutritionTargetEntity e) {
        return NutritionTargetResponse.builder()
                .kcalGoal(e.getKcalGoal())
                .proteinGMin(e.getProteinGMin())
                .proteinGMax(e.getProteinGMax())
                .carbsGMin(e.getCarbsGMin())
                .carbsGMax(e.getCarbsGMax())
                .fatGMin(e.getFatGMin())
                .fatGMax(e.getFatGMax())
                .sugarMax(e.getSugarMax())
                .fiberMin(e.getFiberMin())
                .sodiumMax(e.getSodiumMax())
                .build();
    }
}
