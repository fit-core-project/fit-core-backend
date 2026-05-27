package com.fitcore.api.domain.exercise.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.exercise.request.ExerciseTierRequest;
import com.fitcore.api.domain.exercise.response.AdminExerciseTierResponse;
import com.fitcore.api.domain.exercise.service.ExerciseTierService;

@RestController
@RequestMapping("/api/admin/exercises")
@RequiredArgsConstructor
public class ExerciseTierAdminController {

    private final ExerciseTierService exerciseTierService;

    @GetMapping
    public ResponseEntity<List<AdminExerciseTierResponse>> getAll() {
        return ResponseEntity.ok(exerciseTierService.getAllExercisesForAdmin());
    }

    @PostMapping
    public ResponseEntity<AdminExerciseTierResponse> create(@Valid @RequestBody ExerciseTierRequest request) {
        return ResponseEntity.ok(exerciseTierService.createExercise(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminExerciseTierResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ExerciseTierRequest request) {
        return ResponseEntity.ok(exerciseTierService.updateExercise(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseTierService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }
}
