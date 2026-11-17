package com.fitcore.api.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fitcore.api.domain.user.dto.BodyComposition;
import com.fitcore.api.domain.user.dto.PainAreas;
import com.fitcore.api.domain.user.enums.ExperienceLevel;
import com.fitcore.api.domain.user.enums.Gender;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.UserStatus;
import com.fitcore.api.domain.user.request.UserProfileUpdateRequest;

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

    private UserProfileEntity fullyPopulatedUser() {
        UserProfileEntity user = UserProfileEntity.builder()
            .email("merge@fitcore.com")
            .name("Merge User")
            .status(UserStatus.ACTIVE)
            .build();

        UserProfileUpdateRequest full = new UserProfileUpdateRequest();
        full.setNickname("헬린이");
        full.setGender(Gender.FEMALE);
        full.setBirthDate(LocalDate.of(1995, 3, 2));
        full.setNotes("무릎 조심");
        full.setGoalType(GoalType.hypertrophy);
        full.setExperienceLevel(ExperienceLevel.intermediate);
        full.setTrainingDaysPerWeek(4);
        full.setBodyWeightKg(new BigDecimal("61.5"));
        full.setBodyFatPct(new BigDecimal("24.0"));
        full.setEquipmentAccess(List.of("BARBELL", "DUMBBELL"));
        full.setPainAreas(List.of(new PainAreas()));
        full.setBodyCompositionSnapshot(List.of(BodyComposition.builder()
            .measuredAt(LocalDate.of(2026, 11, 1))
            .bodyWeightKg(61.5)
            .build()));
        user.updateProfile(full);
        return user;
    }

    @Test
    void updateProfile_partialRequest_keepsUnsentFields() {
        UserProfileEntity user = fullyPopulatedUser();
        int versionBefore = user.getProfileVersion();

        // StepWorkout / WorkoutSettingsSection 처럼 일부 필드만 보낸다.
        UserProfileUpdateRequest partial = new UserProfileUpdateRequest();
        partial.setGoalType(GoalType.strength);
        partial.setTrainingDaysPerWeek(5);
        user.updateProfile(partial);

        assertThat(user.getGoalType()).isEqualTo(GoalType.strength);
        assertThat(user.getTrainingDaysPerWeek()).isEqualTo(5);
        assertThat(user.getNickname()).isEqualTo("헬린이");
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1995, 3, 2));
        assertThat(user.getNotes()).isEqualTo("무릎 조심");
        assertThat(user.getExperienceLevel()).isEqualTo(ExperienceLevel.intermediate);
        assertThat(user.getBodyWeightKg()).isEqualByComparingTo("61.5");
        assertThat(user.getBodyFatPct()).isEqualByComparingTo("24.0");
        assertThat(user.getEquipmentAccess()).containsExactly("BARBELL", "DUMBBELL");
        assertThat(user.getPainAreas()).hasSize(1);
        assertThat(user.getBodyCompositionSnapshot()).hasSize(1);
        assertThat(user.getProfileVersion()).isEqualTo(versionBefore + 1);
    }

    @Test
    void updateProfile_emptyList_clearsStoredList() {
        UserProfileEntity user = fullyPopulatedUser();

        UserProfileUpdateRequest clear = new UserProfileUpdateRequest();
        clear.setPainAreas(List.of());
        clear.setBodyCompositionSnapshot(List.of());
        user.updateProfile(clear);

        assertThat(user.getPainAreas()).isEmpty();
        assertThat(user.getBodyCompositionSnapshot()).isEmpty();
        assertThat(user.getEquipmentAccess()).containsExactly("BARBELL", "DUMBBELL");
        assertThat(user.getNickname()).isEqualTo("헬린이");
    }

    @Test
    void updateProfile_nullProfileVersion_startsCounting() {
        UserProfileEntity user = fullyPopulatedUser();
        ReflectionTestUtils.setField(user, "profileVersion", null);

        user.updateProfile(new UserProfileUpdateRequest());

        assertThat(user.getProfileVersion()).isEqualTo(2);
    }

    @Test
    void withdraw_anonymizesEmailAndClearsPersonalData() {
        UserProfileEntity user = fullyPopulatedUser();
        ReflectionTestUtils.setField(user, "userId", "user-123");

        user.withdraw("127.0.0.1");

        assertThat(user.getEmail()).isEqualTo("withdrawn+user-123@withdrawn.fitcore.invalid");
        assertThat(user.getNickname()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getBirthDate()).isNull();
        assertThat(user.getBodyCompositionSnapshot()).isNull();
        assertThat(user.getPainAreas()).isNull();
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getDeletedIp()).isEqualTo("127.0.0.1");
    }
}
