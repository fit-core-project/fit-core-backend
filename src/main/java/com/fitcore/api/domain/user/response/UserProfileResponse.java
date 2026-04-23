package com.fitcore.api.domain.user.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fitcore.api.domain.user.dto.PainAreas;
import com.fitcore.api.domain.user.dto.StrengthBaseline;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.ExperienceLevel;
import com.fitcore.api.domain.user.enums.Gender;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.SplitType;
import com.fitcore.api.domain.user.enums.UserRole;
import com.fitcore.api.domain.user.enums.UserStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private Gender gender;
    private LocalDate birthDate;
    private UserStatus status;
    private String notes;
    private Set<UserRole> roles = new HashSet<>();
    private List<String> linkedProviders;
    private GoalType goalType;
    private SplitType splitType;
    private ExperienceLevel experienceLevel;
    private Integer trainingDaysPerWeek;
    private String splitLabel;
    private BigDecimal bodyWeightKg;
    private BigDecimal bodyFatPct;
    private List<String> availableDays;
    private List<String> equipmentAccess;
    private List<String> unpreferredExerciseIds;
    private List<String> preferredExerciseIds;
    private List<PainAreas> painAreas;
    private Map<String, List<StrengthBaseline>> strengthBaseline;
    private Integer profileVersion;

    public UserProfileResponse(UserProfileEntity entity, List<String> linkedProviders) {
        this.email = entity.getEmail();
        this.name = entity.getName();
        this.nickname = entity.getNickname();
        this.profileImageUrl = entity.getProfileImageUrl();
        this.gender = entity.getGender();
        this.birthDate = entity.getBirthDate();
        this.status = entity.getStatus();
        this.notes = entity.getNotes();
        this.roles = entity.getRoles();
        this.linkedProviders = linkedProviders;
        this.goalType = entity.getGoalType();
        this.splitType = entity.getSplitType();
        this.experienceLevel = entity.getExperienceLevel();
        this.trainingDaysPerWeek = entity.getTrainingDaysPerWeek();
        this.splitLabel = entity.getSplitLabel();
        this.bodyWeightKg = entity.getBodyWeightKg();
        this.bodyFatPct = entity.getBodyFatPct();
        this.availableDays = entity.getAvailableDays();
        this.equipmentAccess = entity.getEquipmentAccess();
        this.unpreferredExerciseIds = entity.getUnpreferredExerciseIds();
        this.preferredExerciseIds = entity.getPreferredExerciseIds();
        this.painAreas = entity.getPainAreas();
        this.strengthBaseline = entity.getStrengthBaseline();
        this.profileVersion = entity.getProfileVersion();
    }
}
