package com.fitcore.api.domain.nutrition.repository;

import com.fitcore.api.domain.nutrition.entity.NutritionTargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionTargetRepository extends JpaRepository<NutritionTargetEntity, String> {
    Optional<NutritionTargetEntity> findByUserId(String userId);
}
