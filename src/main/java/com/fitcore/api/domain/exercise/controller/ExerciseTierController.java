package com.fitcore.api.domain.exercise.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.exercise.response.ExerciseTierResponse;
import com.fitcore.api.domain.exercise.response.RecentRecordResponse;
import com.fitcore.api.domain.exercise.service.ExerciseTierService;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseTierController {
    private final ExerciseTierService exerciseTierService;

    @GetMapping("/catalog")
    public ResponseEntity<List<ExerciseTierResponse>> getExerciseCatalog() {
        List<ExerciseTierResponse> catalog = exerciseTierService.getAllExerciseTiers();
        return ResponseEntity.ok(catalog);
    }

    @GetMapping("/{exerciseId}/recent-record")
    public ResponseEntity<RecentRecordResponse> getRecentRecord(@PathVariable String exerciseId) {
        return ResponseEntity.ok(exerciseTierService.getRecentRecord(exerciseId));
    }
}