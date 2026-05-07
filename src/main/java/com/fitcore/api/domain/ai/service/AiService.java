package com.fitcore.api.domain.ai.service;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fitcore.api.infrastructure.ai.client.AiClient;

@Service
@RequiredArgsConstructor
public class AiService {
    private final AiClient aiClient;

    public void supplementChat(String question) {
        Map<String, Object> map = aiClient.generateSupplementChat(question);
        System.out.println(map); // 응답 확인
    }
}
