package com.fitcore.api.domain.user.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fitcore.api.domain.user.dto.BodyComposition;
import com.fitcore.api.domain.user.dto.PainAreas;
import com.fitcore.api.domain.user.dto.StrengthBaseline;
import com.fitcore.api.domain.user.enums.ExperienceLevel;
import com.fitcore.api.domain.user.enums.Gender;
import com.fitcore.api.domain.user.enums.GoalType;
import com.fitcore.api.domain.user.enums.SplitType;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String nickname;
    private Gender gender;
    private LocalDate birthDate;
    private String notes;

    // 피트니스 관련 정보
    private GoalType goalType;
    private SplitType splitType;
    private ExperienceLevel experienceLevel;
    private Integer trainingDaysPerWeek;
    private String splitLabel;
    private BigDecimal bodyWeightKg;
    private BigDecimal bodyFatPct;

    // JSON 타입 필드 (List/Map)
    private List<String> availableDays;
    private List<String> equipmentAccess;
    private List<String> unpreferredExerciseIds;
    private List<String> preferredExerciseIds;
    private List<PainAreas> painAreas;
    private List<StrengthBaseline> strengthBaseline;
    private List<BodyComposition> bodyCompositionSnapshot;
}
