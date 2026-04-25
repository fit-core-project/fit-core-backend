package com.fitcore.api.domain.workout.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.workout.request.WorkoutSessionRequest;
import com.fitcore.api.domain.workout.response.WorkoutSessionResponse;
import com.fitcore.api.domain.workout.service.WorkoutSessionService;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Tag(name = "Workout Session API", description = "운동 세션 기록 및 조회 관련 API")
public class WorkoutSessionController {
    private final WorkoutSessionService workoutSessionService;

    @GetMapping("/recent")
    @Operation(summary = "최근 운동 세션 조회", description = "로그인한 사용자의 최근 운동 기록을 페이지 단위로 조회합니다.")
    public ResponseEntity<Page<WorkoutSessionResponse>> getRecentWorkouts(
        @PageableDefault(size = 10, sort = "workoutDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(workoutSessionService.getWorkoutSessions(pageable));
    }

    @PostMapping
    @Operation(summary = "운동 세션 생성", description = "새로운 운동 세션 기록을 생성하고 저장합니다.")
    public ResponseEntity<WorkoutSessionResponse> createWorkout(@RequestBody WorkoutSessionRequest request) {
        return ResponseEntity.ok(workoutSessionService.createWorkoutSession(request));
    }
}
