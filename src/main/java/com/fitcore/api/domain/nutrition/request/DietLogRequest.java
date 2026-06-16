package com.fitcore.api.domain.nutrition.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DietLogRequest {

    @NotNull
    private LocalDate logDate;

    private String mealType;

    private String loggedAt; // "HH:mm" nullable

    @NotBlank
    private String foodName;

    @DecimalMin("0.0") @DecimalMax("5000.0")
    private BigDecimal amountG;

    private String amountRaw;

    @Min(0) @Max(5000)
    private Integer kcal;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal proteinG;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal carbsG;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private BigDecimal fatG;

    @NotBlank
    private String source;
}
