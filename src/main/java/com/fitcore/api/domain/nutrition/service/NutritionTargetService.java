package com.fitcore.api.domain.nutrition.service;

import com.fitcore.api.domain.nutrition.entity.NutritionTargetEntity;
import com.fitcore.api.domain.nutrition.repository.NutritionTargetRepository;
import com.fitcore.api.domain.nutrition.request.NutritionTargetRequest;
import com.fitcore.api.domain.nutrition.response.NutritionTargetResponse;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NutritionTargetService {

    private final NutritionTargetRepository nutritionTargetRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public Optional<NutritionTargetResponse> getTarget() {
        String userId = securityUtils.getCurrentUserId();
        return nutritionTargetRepository.findByUserId(userId)
                .map(NutritionTargetResponse::fromEntity);
    }

    @Transactional
    public NutritionTargetResponse upsert(NutritionTargetRequest req) {
        validateMinMax(req.getProteinGMin(), req.getProteinGMax());
        validateMinMax(req.getCarbsGMin(), req.getCarbsGMax());
        validateMinMax(req.getFatGMin(), req.getFatGMax());

        String userId = securityUtils.getCurrentUserId();
        String existingId = nutritionTargetRepository.findByUserId(userId)
                .map(NutritionTargetEntity::getId)
                .orElse(null);

        NutritionTargetEntity entity = NutritionTargetEntity.builder()
                .id(existingId)
                .userId(userId)
                .kcalGoal(req.getKcalGoal())
                .proteinGMin(req.getProteinGMin())
                .proteinGMax(req.getProteinGMax())
                .carbsGMin(req.getCarbsGMin())
                .carbsGMax(req.getCarbsGMax())
                .fatGMin(req.getFatGMin())
                .fatGMax(req.getFatGMax())
                .sugarMax(req.getSugarMax())
                .fiberMin(req.getFiberMin())
                .sodiumMax(req.getSodiumMax())
                .build();

        return NutritionTargetResponse.fromEntity(nutritionTargetRepository.save(entity));
    }

    private void validateMinMax(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
