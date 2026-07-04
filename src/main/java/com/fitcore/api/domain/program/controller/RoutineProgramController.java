package com.fitcore.api.domain.program.controller;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.program.request.ProgramCreateRequest;
import com.fitcore.api.domain.program.response.ProgramDetailResponse;
import com.fitcore.api.domain.program.response.ProgramSummaryResponse;
import com.fitcore.api.domain.program.service.RoutineProgramService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/programs")
public class RoutineProgramController {
    private final RoutineProgramService programService;

    @PostMapping
    public ResponseEntity<ProgramDetailResponse> createProgram(@Valid @RequestBody ProgramCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.createProgram(request));
    }

    @GetMapping("/active")
    public ResponseEntity<ProgramSummaryResponse> getActiveProgram() {
        return programService.getActiveProgram()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ProgramDetailResponse> getProgram(@PathVariable String programId) {
        return ResponseEntity.ok(programService.getProgram(programId));
    }

    @GetMapping
    public ResponseEntity<List<ProgramSummaryResponse>> getPrograms() {
        return ResponseEntity.ok(programService.getPrograms());
    }

    @DeleteMapping("/{programId}")
    public ResponseEntity<ProgramDetailResponse> archiveProgram(@PathVariable String programId) {
        return ResponseEntity.ok(programService.archiveProgram(programId));
    }
}
