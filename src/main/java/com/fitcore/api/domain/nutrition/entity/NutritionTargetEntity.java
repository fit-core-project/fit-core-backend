package com.fitcore.api.domain.nutrition.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UuidGenerator;

import com.fitcore.api.global.common.entity.BaseDeleteEntity;

@Entity
@Table(name = "nutrition_targets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SoftDelete(columnName = "is_deleted")
public class NutritionTargetEntity extends BaseDeleteEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, length = 36, unique = true)
    private String userId;

    @Column(name = "kcal_goal")
    private Integer kcalGoal;

    @Column(name = "protein_g_min", precision = 6, scale = 1)
    private BigDecimal proteinGMin;

    @Column(name = "protein_g_max", precision = 6, scale = 1)
    private BigDecimal proteinGMax;

    @Column(name = "carbs_g_min", precision = 6, scale = 1)
    private BigDecimal carbsGMin;

    @Column(name = "carbs_g_max", precision = 6, scale = 1)
    private BigDecimal carbsGMax;

    @Column(name = "fat_g_min", precision = 6, scale = 1)
    private BigDecimal fatGMin;

    @Column(name = "fat_g_max", precision = 6, scale = 1)
    private BigDecimal fatGMax;

    @Column(name = "sugar_max", precision = 6, scale = 1)
    private BigDecimal sugarMax;

    @Column(name = "fiber_min", precision = 6, scale = 1)
    private BigDecimal fiberMin;

    @Column(name = "sodium_max")
    private Integer sodiumMax;
}
