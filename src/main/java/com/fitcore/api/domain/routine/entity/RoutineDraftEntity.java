package com.fitcore.api.domain.routine.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "routine_drafts", indexes = {
    @Index(name = "idx_routine_drafts_user_created", columnList = "user_id, created_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineDraftEntity {

    @Id
    @UuidGenerator
    @Column(name = "routine_draft_id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "source_profile_version", nullable = false)
    private int sourceProfileVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_workout_session_ids")
    private Map<String, Object> sourceWorkoutSessionIds;

    @Column(name = "target_split_label", nullable = false, length = 64)
    private String targetSplitLabel;

    @Column(name = "generation_status", nullable = false, length = 16)
    private String generationStatus;

    @Column(name = "status_reason_code", nullable = false, length = 32)
    private String statusReasonCode;

    @Column(name = "is_fallback", nullable = false)
    private boolean isFallback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload_snapshot", nullable = false)
    private Map<String, Object> requestPayloadSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload_snapshot", nullable = false)
    private Map<String, Object> responsePayloadSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adapter_request_snapshot")
    private Map<String, Object> adapterRequestSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adapter_response_snapshot")
    private Map<String, Object> adapterResponseSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rationale_summary", nullable = false)
    private Map<String, Object> rationaleSummary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
