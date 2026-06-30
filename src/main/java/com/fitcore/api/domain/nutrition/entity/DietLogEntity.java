package com.fitcore.api.domain.nutrition.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UuidGenerator;

import com.fitcore.api.global.common.entity.BaseDeleteEntity;

@Entity
@Table(name = "diet_logs", indexes = {
    @Index(name = "idx_diet_logs_user_date", columnList = "user_id, log_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SoftDelete(columnName = "is_deleted")
public class DietLogEntity extends BaseDeleteEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "meal_type", length = 20)
    private String mealType;

    @Column(name = "logged_at")
    private LocalDateTime loggedAt;

    @Column(name = "food_name", nullable = false, length = 255)
    private String foodName;

    @Column(name = "amount_g", precision = 8, scale = 1)
    private BigDecimal amountG;

    @Column(name = "amount_raw", length = 50)
    private String amountRaw;

    @Column(name = "kcal", nullable = false)
    private Integer kcal;

    @Column(name = "protein_g", precision = 6, scale = 1)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", precision = 6, scale = 1)
    private BigDecimal carbsG;

    @Column(name = "fat_g", precision = 6, scale = 1)
    private BigDecimal fatG;

    @Column(name = "sugar_g", precision = 6, scale = 1)
    private BigDecimal sugarG;

    @Column(name = "fiber_g", precision = 6, scale = 1)
    private BigDecimal fiberG;

    @Column(name = "sodium_mg")
    private Integer sodiumMg;

    @Column(name = "source", nullable = false, length = 20)
    private String source;
}
