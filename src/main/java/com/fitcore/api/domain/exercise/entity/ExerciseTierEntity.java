package com.fitcore.api.domain.exercise.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise_tier")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExerciseTierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_kr", columnDefinition = "TEXT")
    private String nameKr;

    @Column(name = "name_en", columnDefinition = "TEXT")
    private String nameEn;

    @Column(name = "primary_muscle", columnDefinition = "TEXT")
    private String primaryMuscle;

    @Column(name = "secondary_muscle", columnDefinition = "TEXT")
    private String secondaryMuscle;

    @Column(name = "equipment_req", columnDefinition = "TEXT")
    private String equipmentReq;

    @Column(name = "difficulty_tier")
    private Long difficultyTier;

    @Column(name = "efficiency_tier")
    private Long efficiencyTier;

    @Column(name = "pain_triggers", columnDefinition = "TEXT")
    private String painTriggers;

    @Column(name = "movement_type", columnDefinition = "TEXT")
    private String movementType;
}
