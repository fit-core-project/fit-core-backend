package com.fitcore.api.domain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    // AiService 대신 RestClientConfig에서 만든 통신 도구를 바로 주입받습니다.
    private final RestClient aiRestClient;

    @PostMapping("/supplement-chat")
    public ResponseEntity<String> supplementChat(@RequestBody String requestBody) {
        return forwardToPythonServer("/supplement-chat", requestBody);
    }

    @PostMapping("/generate-routine")
    public ResponseEntity<String> generateRoutine(@RequestBody String requestBody) {
        return forwardToPythonServer("/generate-routine", requestBody);
    }

    @PostMapping("/parse-log")
    public ResponseEntity<String> parseLog(@RequestBody String requestBody) {
        return forwardToPythonServer("/parse-log", requestBody);
    }

    // 중복 코드를 줄이기 위한 내부 프록시 메서드
    private ResponseEntity<String> forwardToPythonServer(String path, String body) {
        ResponseEntity<String> response = aiRestClient.post()
            .uri(path) // 파이썬 서버의 동일한 경로로 토스
            .body(body)
            .retrieve()
            .toEntity(String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
