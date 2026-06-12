package com.fitcore.api.infrastructure.ai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fitcore.api.infrastructure.ai.dto.AiRoutineRequest;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiClient {
    private final RestClient aiRestClient;

    public AiRoutineResponse generateRoutine(AiRoutineRequest aiRequest) {
        log.info("event=ai_request endpoint=generate-routine");
        return aiRestClient.post()
            .uri("/generate-routine")
            .contentType(MediaType.APPLICATION_JSON)
            .body(aiRequest)
            .retrieve()
            .body(AiRoutineResponse.class);
    }

    public Map<String, Object> generateSupplementChat(String question) {
        log.info("event=ai_request endpoint=supplement-chat");
        return aiRestClient.post()
            .uri("/supplement-chat")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("question", question))
            .retrieve()
            .body(Map.class);
    }
}
