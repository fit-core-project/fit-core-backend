package com.fitcore.api.domain.nutrition.controller;

import com.fitcore.api.domain.nutrition.request.NutritionTargetRequest;
import com.fitcore.api.domain.nutrition.response.NutritionTargetResponse;
import com.fitcore.api.domain.nutrition.service.NutritionTargetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition-targets")
@RequiredArgsConstructor
public class NutritionTargetController {

    private final NutritionTargetService nutritionTargetService;

    @GetMapping("/me")
    public ResponseEntity<NutritionTargetResponse> getMyTarget() {
        return nutritionTargetService.getTarget()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/me")
    public ResponseEntity<NutritionTargetResponse> upsertMyTarget(
            @RequestBody @Valid NutritionTargetRequest request) {
        return ResponseEntity.ok(nutritionTargetService.upsert(request));
    }
}
