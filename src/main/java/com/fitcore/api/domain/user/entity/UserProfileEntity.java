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

//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
//    @Column(name = "role_name")
//    @Enumerated(EnumType.STRING) // Enum 문자열 저장을 위해 필수 추가
//    private Set<UserRole> roles = new HashSet<>();

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
//        this.roles = (roles != null) ? roles : new HashSet<>(Collections.singleton(UserRole.ROLE_USER));
    }

    public void updateProfile(UserProfileUpdateRequest request) {
        // 1. 기본 정보 업데이트
        this.nickname = request.getNickname();
        this.gender = request.getGender();
        this.birthDate = request.getBirthDate();
        this.notes = request.getNotes();

        // 2. 피트니스 정보 업데이트
        this.goalType = request.getGoalType();
        this.splitType = request.getSplitType();
        this.experienceLevel = request.getExperienceLevel();
        this.trainingDaysPerWeek = request.getTrainingDaysPerWeek();
        this.splitLabel = request.getSplitLabel();
        this.bodyWeightKg = request.getBodyWeightKg();
        this.bodyFatPct = request.getBodyFatPct();

        // 3. JSON 타입 데이터 업데이트 (Hibernate가 자동으로 처리)
        this.availableDays = request.getAvailableDays();
        this.equipmentAccess = request.getEquipmentAccess();
        this.unpreferredExerciseIds = request.getUnpreferredExerciseIds();
        this.preferredExerciseIds = request.getPreferredExerciseIds();
        this.painAreas = request.getPainAreas();
        this.strengthBaseline = request.getStrengthBaseline();
        this.bodyCompositionSnapshot = request.getBodyCompositionSnapshot();

        // 필요 시 버전 업데이트 (선택 사항)
        this.profileVersion += 1;
    }

    /**
     * 탈퇴 처리
     */
    public void withdraw(String ip) {
        this.markAsDeleted(ip);
    }
}
