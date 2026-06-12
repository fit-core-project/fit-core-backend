package com.fitcore.api.domain.user.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fitcore.api.domain.user.dto.StrengthBaseline;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.SplitType;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserPreferencesResponse {
    private Integer timeAvailable;
    private GoalType goal;
    private List<String> equipment;
    private int weeklyFrequency;
    private SplitType splitPreference;
    private Map<String, BigDecimal> baselineWeights;

    public UserPreferencesResponse(UserProfileEntity entity) {
        this.timeAvailable = entity.getEffectiveTimeAvailable();
        this.goal = entity.getEffectiveGoalType();
        this.equipment = entity.getEffectiveEquipmentAccess();
        this.weeklyFrequency = entity.getEffectiveTrainingDaysPerWeek();
        this.splitPreference = entity.getSplitType();
        this.baselineWeights = Optional.ofNullable(entity.getStrengthBaseline())
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(base -> base.getExerciseNameSnapshot() != null)
            .collect(Collectors.toMap(
                StrengthBaseline::getExerciseNameSnapshot,
                base -> base.getWorkingWeightKg() != null ? base.getWorkingWeightKg() : BigDecimal.ZERO,
                (existing, replacement) -> existing
            ));
    }
}
