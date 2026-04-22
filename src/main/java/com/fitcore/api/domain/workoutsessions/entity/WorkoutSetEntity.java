package com.fitcore.api.domain.workoutsessions.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "workout_sets", indexes = {
    @Index(name = "idx_workout_sets_session_order", columnList = "workout_session_id, exercise_order, set_index")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkoutSetEntity {
    @Id
    @UuidGenerator
    @Column(name = "workout_set_id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSessionEntity workoutSession;

    @Column(name = "exercise_order")
    private Integer exerciseOrder;

    @Column(name = "exercise_id", length = 128)
    private String exerciseId;

    @Column(name = "exercise_name_snapshot", nullable = false, length = 128)
    private String exerciseNameSnapshot;

    @Column(name = "set_index", nullable = false)
    private Integer setIndex;

    @Column(name = "set_type", nullable = false, length = 16)
    @Builder.Default
    private String setType = "working";

    @Column(name = "tracking_mode", nullable = false, length = 32)
    @Builder.Default
    private String trackingMode = "weightReps";

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "reps", nullable = false)
    private Integer reps;

    @Column(name = "rpe", precision = 3, scale = 1)
    private BigDecimal rpe;

    @Column(name = "rir", precision = 3, scale = 1)
    private BigDecimal rir;

    @Column(name = "is_failure", nullable = false)
    @Builder.Default
    private Boolean isFailure = false;

    @Column(name = "rest_sec")
    private Integer restSec;

    @Column(name = "set_note", columnDefinition = "TEXT")
    private String setNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
