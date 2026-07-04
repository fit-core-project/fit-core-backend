package com.fitcore.api.domain.program.entity;

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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fitcore.api.domain.workout.entity.WorkoutSessionEntity;

@Entity
@Table(
    name = "routine_program_completion_events",
    indexes = {
        @Index(name = "idx_routine_program_events_program", columnList = "program_id, created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_routine_program_events_session", columnNames = "workout_session_id"),
        @UniqueConstraint(
            name = "uq_routine_program_events_program_item_session",
            columnNames = {"program_id", "program_item_id", "workout_session_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineProgramCompletionEventEntity {
    @Id
    @UuidGenerator
    @Column(name = "event_id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private RoutineProgramEntity program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_item_id", nullable = false)
    private RoutineProgramItemEntity programItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSessionEntity workoutSession;

    @Column(name = "completed_position", nullable = false)
    private int completedPosition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
