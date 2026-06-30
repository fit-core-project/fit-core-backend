package com.fitcore.api.domain.nutrition.response;

import com.fitcore.api.domain.nutrition.entity.DietLogEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DietLogResponse {

    private String id;
    private LocalDate logDate;
    private String mealType;
    private LocalDateTime loggedAt;
    private String foodName;
    private BigDecimal amountG;
    private String amountRaw;
    private Integer kcal;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
    private BigDecimal sugarG;
    private BigDecimal fiberG;
    private Integer sodiumMg;
    private String source;

    public static DietLogResponse fromEntity(DietLogEntity entity) {
        return DietLogResponse.builder()
                .id(entity.getId())
                .logDate(entity.getLogDate())
                .mealType(entity.getMealType())
                .loggedAt(entity.getLoggedAt())
                .foodName(entity.getFoodName())
                .amountG(entity.getAmountG())
                .amountRaw(entity.getAmountRaw())
                .kcal(entity.getKcal())
                .proteinG(entity.getProteinG())
                .carbsG(entity.getCarbsG())
                .fatG(entity.getFatG())
                .sugarG(entity.getSugarG())
                .fiberG(entity.getFiberG())
                .sodiumMg(entity.getSodiumMg())
                .source(entity.getSource())
                .build();
    }
}
