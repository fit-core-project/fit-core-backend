package com.fitcore.api.domain.user.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.UserStatus;

class UserPreferencesResponseTest {

    @Test
    void constructor_usesDefaultsWhenProfilePreferencesAreNull() {
        UserProfileEntity user = UserProfileEntity.builder()
            .email("new-user@fitcore.com")
            .name("New User")
            .status(UserStatus.ACTIVE)
            .build();
        ReflectionTestUtils.setField(user, "timeAvailable", null);
        ReflectionTestUtils.setField(user, "goalType", null);
        ReflectionTestUtils.setField(user, "equipmentAccess", null);
        ReflectionTestUtils.setField(user, "trainingDaysPerWeek", null);

        UserPreferencesResponse response = new UserPreferencesResponse(user);

        assertThat(response.getTimeAvailable()).isEqualTo(60);
        assertThat(response.getGoal()).isEqualTo(GoalType.generalFitness);
        assertThat(response.getEquipment()).isEmpty();
        assertThat(response.getWeeklyFrequency()).isEqualTo(3);
        assertThat(response.getSplitPreference()).isNull();
        assertThat(response.getBaselineWeights()).isEmpty();
    }
}
