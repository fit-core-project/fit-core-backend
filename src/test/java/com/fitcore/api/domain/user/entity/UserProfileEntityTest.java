package com.fitcore.api.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.UserStatus;

class UserProfileEntityTest {

    @Test
    void builder_setsPreferenceDefaultsForNewOAuthUsers() {
        UserProfileEntity user = UserProfileEntity.builder()
            .email("oauth-user@fitcore.com")
            .name("OAuth User")
            .status(UserStatus.ACTIVE)
            .build();

        assertThat(user.getTimeAvailable()).isEqualTo(UserProfileEntity.DEFAULT_TIME_AVAILABLE);
        assertThat(user.getTrainingDaysPerWeek()).isEqualTo(UserProfileEntity.DEFAULT_TRAINING_DAYS_PER_WEEK);
        assertThat(user.getGoalType()).isEqualTo(GoalType.generalFitness);
        assertThat(user.getEquipmentAccess()).isEmpty();
    }
}
