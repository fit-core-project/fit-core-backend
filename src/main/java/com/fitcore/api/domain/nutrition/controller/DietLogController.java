package com.fitcore.api.domain.nutrition.controller;

import com.fitcore.api.domain.nutrition.request.DietLogRequest;
import com.fitcore.api.domain.nutrition.request.DietLogUpdateRequest;
import com.fitcore.api.domain.nutrition.response.DietDailyAggregationResponse;
import com.fitcore.api.domain.nutrition.response.DietLogResponse;
import com.fitcore.api.domain.nutrition.response.DietSummaryResponse;
import com.fitcore.api.domain.nutrition.service.DietLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diet-logs")
@RequiredArgsConstructor
public class DietLogController {

    private final DietLogService dietLogService;

    @PostMapping
    public ResponseEntity<List<DietLogResponse>> saveBatch(
            @RequestBody @Valid List<@Valid DietLogRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dietLogService.saveBatch(requests));
    }

    @GetMapping("/summary")
    public ResponseEntity<DietSummaryResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dietLogService.getSummary(date));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<DietDailyAggregationResponse>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dietLogService.getDailyAggregation(from, to));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        dietLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietLogResponse> update(
            @PathVariable String id,
            @RequestBody @Valid DietLogUpdateRequest request) {
        return ResponseEntity.ok(dietLogService.update(id, request));
    }
}
