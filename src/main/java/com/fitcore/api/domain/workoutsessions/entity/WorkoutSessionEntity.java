package com.fitcore.api.domain.workoutsessions.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.fitcore.api.domain.workoutsessions.dto.CurrentPainAreas;
import com.fitcore.api.domain.workoutsessions.dto.Doms;

@Entity
@Table(name = "workout_sessions", indexes = {
    @Index(name = "idx_workout_sessions_user_date", columnList = "user_id, workout_date DESC"),
    @Index(name = "idx_workout_sessions_source_final", columnList = "source_routine_final_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkoutSessionEntity {
    @Id
    @UuidGenerator
    @Column(name = "workout_session_id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @Column(name = "split_label", length = 64)
    private String splitLabel;

    @Column(name = "source_routine_final_id", length = 36)
    private String sourceRoutineFinalId;

    @Column(name = "time_available_min")
    private Short timeAvailableMin;

    @Column(name = "duration_min")
    private Short durationMin;

    @Column(name = "readiness_level", nullable = false, length = 16)
    @Builder.Default
    private String readinessLevel = "normal";

    // JSON 처리 (Map 혹은 커스텀 DTO 사용 가능)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_pain_areas", columnDefinition = "json")
    private List<CurrentPainAreas> currentPainAreas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "doms", columnDefinition = "json")
    private List<Doms> doms;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unavailable_equipment", columnDefinition = "json")
    private Map<String, Object> unavailableEquipment;

    @Column(name = "session_note", columnDefinition = "TEXT")
    private String sessionNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 연관관계 매핑 (WorkoutSets)
    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutSetEntity> workoutSets = new ArrayList<>();
}
