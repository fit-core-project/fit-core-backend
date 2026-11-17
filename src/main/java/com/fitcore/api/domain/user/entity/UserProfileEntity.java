package com.fitcore.api.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.fitcore.api.domain.routine.dto.Doms;
import com.fitcore.api.domain.user.dto.BodyComposition;
import com.fitcore.api.domain.user.dto.PainAreas;
import com.fitcore.api.domain.user.dto.StrengthBaseline;
import com.fitcore.api.domain.user.enums.ExperienceLevel;
import com.fitcore.api.domain.user.enums.Gender;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.SplitType;
import com.fitcore.api.domain.user.enums.UserRole;
import com.fitcore.api.domain.user.enums.UserStatus;
import com.fitcore.api.domain.user.request.UserProfileUpdateRequest;
import com.fitcore.api.global.common.entity.BaseDeleteEntity;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(columnName = "is_deleted")
public class UserProfileEntity extends BaseDeleteEntity {
    public static final int DEFAULT_TIME_AVAILABLE = 60;
    public static final int DEFAULT_TRAINING_DAYS_PER_WEEK = 3;
    public static final GoalType DEFAULT_GOAL_TYPE = GoalType.generalFitness;

    @Id
    @UuidGenerator
    @Column(name = "user_id", length = 36, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // --- ENUM 필드 ---
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type")
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type")
    private SplitType splitType;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    // --- 기본 정보 ---
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "training_days_per_week")
    private Integer trainingDaysPerWeek;

    @Column(name = "split_label")
    private String splitLabel;

    @Column(name = "body_weight_kg", precision = 5, scale = 2)
    private BigDecimal bodyWeightKg;

    @Column(name = "body_fat_pct", precision = 5, scale = 2)
    private BigDecimal bodyFatPct;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "profile_version")
    private Integer profileVersion = 1;

    @Column(name = "time_available")
    private Integer timeAvailable;

    // --- JSON 필드 매핑 ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_days", columnDefinition = "JSON")
    private List<String> availableDays;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "equipment_access", columnDefinition = "JSON")
    private List<String> equipmentAccess;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unpreferred_exercise_ids", columnDefinition = "JSON")
    private List<String> unpreferredExerciseIds;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_exercise_ids", columnDefinition = "JSON")
    private List<String> preferredExerciseIds;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pain_areas", columnDefinition = "JSON")
    private List<PainAreas> painAreas;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "doms", columnDefinition = "JSON")
    private List<Doms> doms;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strength_baseline", columnDefinition = "JSON")
    private List<StrengthBaseline> strengthBaseline;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "body_composition_snapshot", columnDefinition = "JSON")
    private List<BodyComposition> bodyCompositionSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private UserRole role = UserRole.ROLE_USER;

    @Builder
    public UserProfileEntity(
        String email, String name, String nickname, String profileImageUrl,
        Gender gender, LocalDate birthDate, UserStatus status, Set<UserRole> roles) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.gender = gender;
        this.birthDate = birthDate;
        this.status = (status != null) ? status : UserStatus.ACTIVE;
        this.role = UserRole.ROLE_USER;
        applyPreferenceDefaults();
    }

    public void applyPreferenceDefaults() {
        if (this.timeAvailable == null) {
            this.timeAvailable = DEFAULT_TIME_AVAILABLE;
        }
        if (this.trainingDaysPerWeek == null) {
            this.trainingDaysPerWeek = DEFAULT_TRAINING_DAYS_PER_WEEK;
        }
        if (this.goalType == null) {
            this.goalType = DEFAULT_GOAL_TYPE;
        }
        if (this.equipmentAccess == null) {
            this.equipmentAccess = List.of();
        }
    }

    public Integer getEffectiveTimeAvailable() {
        return timeAvailable != null ? timeAvailable : DEFAULT_TIME_AVAILABLE;
    }

    public GoalType getEffectiveGoalType() {
        return goalType != null ? goalType : DEFAULT_GOAL_TYPE;
    }

    public List<String> getEffectiveEquipmentAccess() {
        return equipmentAccess != null ? equipmentAccess : List.of();
    }

    public int getEffectiveTrainingDaysPerWeek() {
        return trainingDaysPerWeek != null ? trainingDaysPerWeek : DEFAULT_TRAINING_DAYS_PER_WEEK;
    }

    /**
     * 부분 업데이트(merge). 요청에서 null인 필드는 저장된 값을 유지한다.
     * 리스트/JSON 필드는 null이면 유지, 빈 리스트면 비운다.
     * 스칼라 필드(birthDate 등)는 이 API로 null로 되돌릴 수 없다.
     */
    public void updateProfile(UserProfileUpdateRequest request) {
        // 1. 기본 정보
        if (request.getNickname() != null) this.nickname = request.getNickname();
        if (request.getGender() != null) this.gender = request.getGender();
        if (request.getBirthDate() != null) this.birthDate = request.getBirthDate();
        if (request.getNotes() != null) this.notes = request.getNotes();

        // 2. 피트니스 정보
        if (request.getGoalType() != null) this.goalType = request.getGoalType();
        if (request.getSplitType() != null) this.splitType = request.getSplitType();
        if (request.getExperienceLevel() != null) this.experienceLevel = request.getExperienceLevel();
        if (request.getTrainingDaysPerWeek() != null) this.trainingDaysPerWeek = request.getTrainingDaysPerWeek();
        if (request.getSplitLabel() != null) this.splitLabel = request.getSplitLabel();
        if (request.getBodyWeightKg() != null) this.bodyWeightKg = request.getBodyWeightKg();
        if (request.getBodyFatPct() != null) this.bodyFatPct = request.getBodyFatPct();

        // 3. JSON 필드: null = 유지, [] = 비우기
        if (request.getAvailableDays() != null) this.availableDays = request.getAvailableDays();
        if (request.getEquipmentAccess() != null) this.equipmentAccess = request.getEquipmentAccess();
        if (request.getUnpreferredExerciseIds() != null) this.unpreferredExerciseIds = request.getUnpreferredExerciseIds();
        if (request.getPreferredExerciseIds() != null) this.preferredExerciseIds = request.getPreferredExerciseIds();
        if (request.getPainAreas() != null) this.painAreas = request.getPainAreas();
        if (request.getStrengthBaseline() != null) this.strengthBaseline = request.getStrengthBaseline();
        if (request.getBodyCompositionSnapshot() != null) this.bodyCompositionSnapshot = request.getBodyCompositionSnapshot();

        this.profileVersion = (this.profileVersion == null ? 1 : this.profileVersion) + 1;
    }

    public void updateDoms(List<Doms> newDoms) {
        this.doms = newDoms;
    }

    /**
     * 탈퇴 처리
     */
    public void withdraw(String ip) {
        this.markAsDeleted(ip);
        // 이메일은 unique 제약이 있어 같은 소셜 계정으로 재가입할 수 있도록 익명화한다.
        this.email = "withdrawn+" + this.userId + "@withdrawn.fitcore.invalid";
        this.name = null;
        this.nickname = null;
        this.profileImageUrl = null;
        this.gender = null;
        this.birthDate = null;
        this.notes = null;
        this.bodyWeightKg = null;
        this.bodyFatPct = null;
        this.availableDays = null;
        this.equipmentAccess = null;
        this.unpreferredExerciseIds = null;
        this.preferredExerciseIds = null;
        this.painAreas = null;
        this.doms = null;
        this.strengthBaseline = null;
        this.bodyCompositionSnapshot = null;
    }
}
