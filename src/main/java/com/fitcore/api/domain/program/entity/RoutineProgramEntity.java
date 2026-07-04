package com.fitcore.api.domain.program.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "routine_programs", indexes = {
    @Index(name = "idx_routine_programs_user_status", columnList = "user_id, status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineProgramEntity {
    @Id
    @UuidGenerator
    @Column(name = "program_id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private RoutineProgramStatus status;

    @Column(name = "current_position", nullable = false)
    private int currentPosition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void advance() {
        this.currentPosition += 1;
    }

    public void complete(LocalDateTime completedAt) {
        this.status = RoutineProgramStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void archive() {
        this.status = RoutineProgramStatus.ARCHIVED;
    }

    public void activate() {
        this.status = RoutineProgramStatus.ACTIVE;
    }

    public void rename(String name) {
        this.name = name;
    }
}
