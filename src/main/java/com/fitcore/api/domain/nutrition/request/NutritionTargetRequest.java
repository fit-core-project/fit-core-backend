package com.fitcore.api.domain.nutrition.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class NutritionTargetRequest {

    @Min(0) @Max(10000)
    private Integer kcalGoal;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal proteinGMin;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal proteinGMax;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal carbsGMin;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal carbsGMax;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal fatGMin;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal fatGMax;

    @DecimalMin("0.0") @DecimalMax("500.0")
    private BigDecimal sugarMax;

    @DecimalMin("0.0") @DecimalMax("200.0")
    private BigDecimal fiberMin;

    @Min(0) @Max(30000)
    private Integer sodiumMax;
}
