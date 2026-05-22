package com.fitcore.api.domain.workout.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.fitcore.api.domain.workout.request.WorkoutSessionRequest;
import com.fitcore.api.domain.workout.response.AttendanceWeekResponse;
import com.fitcore.api.domain.workout.response.PrResponse;
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

    @GetMapping("/{workoutSessionId}")
    @Operation(summary = "운동 세션 조회", description = "특정 운동 세션을 조회합니다.")
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionById(
        @PathVariable String workoutSessionId) {
        return ResponseEntity.ok(workoutSessionService.getWorkoutSessionById(workoutSessionId));
    }

    @PostMapping
    @Operation(summary = "운동 세션 생성", description = "새로운 운동 세션 기록을 생성하고 저장합니다.")
    public ResponseEntity<WorkoutSessionResponse> createWorkout(@Valid @RequestBody WorkoutSessionRequest request) {
        return ResponseEntity.ok(workoutSessionService.createWorkoutSession(request));
    }

    @GetMapping("/prs")
    @Operation(summary = "개인 기록 조회", description = "종목별 추정 1RM 최고 기록을 조회합니다.")
    public ResponseEntity<List<PrResponse>> getPersonalRecords() {
        return ResponseEntity.ok(workoutSessionService.getPrs());
    }

    @GetMapping("/attendance")
    @Operation(summary = "출석률 조회", description = "최근 4주 출석률을 조회합니다.")
    public ResponseEntity<List<AttendanceWeekResponse>> getAttendance() {
        return ResponseEntity.ok(workoutSessionService.getAttendance());
    }
}
