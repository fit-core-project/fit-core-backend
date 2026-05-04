package com.fitcore.api.domain.routine.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.fitcore.api.domain.routine.dto.FinalRoutinePayload;

@Entity
@Table(name = "routine_finals", indexes = {
    @Index(name = "idx_routine_finals_user_saved", columnList = "user_id, saved_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineFinalEntity {

    @Id
    @UuidGenerator
    @Column(name = "routine_final_id", length = 36)
    private String id;

    // RoutineDraft와 1:1 관계 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_draft_id", nullable = false, unique = true)
    private RoutineDraftEntity routineDraft;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "target_workout_date")
    private LocalDate targetWorkoutDate;

    @Column(name = "target_split_label", nullable = false, length = 64)
    private String targetSplitLabel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_routine_payload", nullable = false)
    private FinalRoutinePayload finalRoutinePayload;

    @Column(name = "accepted_without_edits")
    private Boolean acceptedWithoutEdits;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_edit_summary")
    private List<String> userEditSummary;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;
}