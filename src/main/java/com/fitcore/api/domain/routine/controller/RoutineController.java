package com.fitcore.api.domain.routine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.routine.request.RoutineFinalRequest;
import com.fitcore.api.domain.routine.request.RoutineGenerateRequest;
import com.fitcore.api.domain.routine.response.RoutineDraftResponse;
import com.fitcore.api.domain.routine.response.RoutineFinalResponse;
import com.fitcore.api.domain.routine.service.RoutineService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routines")
@Tag(name = "Routine API", description = "루틴 생성, 확정 및 조회 관련 API")
public class RoutineController {
    private final RoutineService routineService;

    @PostMapping("/generate")
    public ResponseEntity<RoutineDraftResponse> generate(
        @Valid @RequestBody RoutineGenerateRequest request) {

        RoutineDraftResponse response = routineService.generateRoutine(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/drafts/{routineDraftId}/finalize")
    @Operation(summary = "루틴 초안 확정", description = "작성된 루틴 초안(Draft)을 기반으로 최종 루틴(Final)을 생성합니다.")
    public ResponseEntity<RoutineFinalResponse> finalizeRoutine(
        @PathVariable String routineDraftId,
        @RequestBody RoutineFinalRequest request) {
        return ResponseEntity.ok(routineService.finalizeRoutine(routineDraftId, request));
    }

    @Operation(summary = "확정 루틴 상세 조회", description = "특정 확정된 루틴(Final)의 상세 정보를 조회합니다.")
    @GetMapping("/finals/{routineFinalId}")
    public ResponseEntity<RoutineFinalResponse> getFinalRoutine(
        @PathVariable String routineFinalId) {
        return ResponseEntity.ok(routineService.getFinalRoutine(routineFinalId));
    }
}

