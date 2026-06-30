package com.fitcore.api.domain.nutrition.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DietSummaryResponse {

    private LocalDate date;
    private Integer totalKcal;
    private BigDecimal totalProteinG;
    private BigDecimal totalCarbsG;
    private BigDecimal totalFatG;
    private BigDecimal totalSugarG;
    private BigDecimal totalFiberG;
    private Integer totalSodiumMg;
    private List<DietLogResponse> items;
}
