package com.fitcore.api.infrastructure.ai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineRequest;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiClient {
    private final RestClient aiRestClient;

    public AiRoutineResponse generateRoutine(AiRoutineRequest aiRequest) {
        log.info("AI 서버로 루틴 생성 요청 발송");
        return aiRestClient.post()
            .uri("/generate-routine")
            .contentType(MediaType.APPLICATION_JSON)
            .body(aiRequest)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                log.error("AI 서버 응답 에러: status={}", res.getStatusCode());
                throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
            })
            .body(AiRoutineResponse.class);
    }
}