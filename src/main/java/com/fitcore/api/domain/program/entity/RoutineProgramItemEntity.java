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

import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;

@Entity
@Table(
    name = "routine_program_items",
    indexes = {
        @Index(name = "idx_routine_program_items_program", columnList = "program_id, position")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_routine_program_items_position", columnNames = {"program_id", "position"}),
        @UniqueConstraint(name = "uq_routine_program_items_final", columnNames = {"program_id", "routine_final_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineProgramItemEntity {
    @Id
    @UuidGenerator
    @Column(name = "program_item_id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private RoutineProgramEntity program;

    @Column(name = "position", nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_final_id", nullable = false)
    private RoutineFinalEntity routineFinal;

    @Column(name = "title_snapshot")
    private String titleSnapshot;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
