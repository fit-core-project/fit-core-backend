package com.fitcore.api.domain.ai.controller;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitcore.api.domain.ai.service.AiService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;


    @PostMapping("/supplement-chat")
    public ResponseEntity<Void> supplementChat(
        @Valid @RequestBody String question) {
        aiService.supplementChat(question);
        return (ResponseEntity<Void>) ResponseEntity.ok();
    }

}
