package com.fitcore.api.domain.nutrition.response;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
public class DietDailyAggregationResponse {

    private final LocalDate date;
    private final Integer totalKcal;
    private final BigDecimal totalCarbsG;
    private final BigDecimal totalProteinG;
    private final BigDecimal totalFatG;
    private final Long count;

    public DietDailyAggregationResponse(
            LocalDate date, Long totalKcal,
            BigDecimal totalCarbsG, BigDecimal totalProteinG, BigDecimal totalFatG,
            Long count) {
        this.date = date;
        this.totalKcal = totalKcal != null ? totalKcal.intValue() : 0;
        this.totalCarbsG = orZero(totalCarbsG);
        this.totalProteinG = orZero(totalProteinG);
        this.totalFatG = orZero(totalFatG);
        this.count = count;
    }

    private static BigDecimal orZero(BigDecimal v) {
        return (v != null ? v : BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP);
    }
}
