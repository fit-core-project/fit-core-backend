package com.fitcore.api.domain.user.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fitcore.api.domain.routine.dto.Doms;
import com.fitcore.api.domain.user.dto.BodyComposition;
import com.fitcore.api.domain.user.dto.PainAreas;
import com.fitcore.api.domain.user.dto.StrengthBaseline;
import com.fitcore.api.domain.user.entity.UserProfileEntity;
import com.fitcore.api.domain.user.enums.ExperienceLevel;
import com.fitcore.api.domain.user.enums.Gender;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.SplitType;
import com.fitcore.api.domain.user.enums.UserStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserProfileResponse {
    private String userId;
    private String email;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private Gender gender;
    private LocalDate birthDate;
    private UserStatus status;
    private Integer timeAvailable;
    private String notes;
    //    private Set<UserRole> roles = new HashSet<>();
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
    private List<Doms> doms;
    private List<StrengthBaseline> strengthBaseline;
    private Integer profileVersion;
    private List<BodyComposition> bodyCompositionSnapshot;

    public UserProfileResponse(UserProfileEntity entity, List<String> linkedProviders) {
        this.userId = entity.getUserId();
        this.email = entity.getEmail();
        this.name = entity.getName();
        this.nickname = entity.getNickname();
        this.profileImageUrl = entity.getProfileImageUrl();
        this.gender = entity.getGender();
        this.birthDate = entity.getBirthDate();
        this.status = entity.getStatus();
        this.timeAvailable = entity.getTimeAvailable();
        this.notes = entity.getNotes();
//        this.roles = entity.getRoles();
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
        this.doms = entity.getDoms();
        this.strengthBaseline = entity.getStrengthBaseline();
        this.profileVersion = entity.getProfileVersion();
        List<BodyComposition> source = entity.getBodyCompositionSnapshot();

        this.bodyCompositionSnapshot = (source == null || source.isEmpty())
            ? null
            : source.stream()
              .sorted(Comparator.comparing(BodyComposition::getMeasuredAt).reversed())
              .collect(Collectors.toList());
    }
}
