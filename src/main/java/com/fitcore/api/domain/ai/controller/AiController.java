package com.fitcore.api.domain.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;
import com.fitcore.api.infrastructure.ai.fallback.AiFailureClassifier;
import com.fitcore.api.infrastructure.ai.fallback.AiFallbackResponseFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@Slf4j
public class AiController {
    private final RestClient aiRestClient;
    private final ObjectMapper objectMapper;
    private final AiFallbackResponseFactory fallbackResponseFactory;

    @Value("${ai.enable-fallback:true}")
    private boolean fallbackEnabled;

    @Value("${ai.server.url:}")
    private String aiServerUrl;

    @Value("${app.env:local}")
    private String appEnv;

    @PostMapping("/supplement-chat")
    public ResponseEntity<String> supplementChat(@RequestBody(required = false) String requestBody) {
        return forwardToPythonServer("/supplement-chat", requestBody, AiEndpoint.SUPPLEMENT);
    }

    @PostMapping("/generate-routine")
    public ResponseEntity<String> generateRoutine(@RequestBody String requestBody) {
        return forwardToPythonServer("/generate-routine", requestBody, AiEndpoint.ROUTINE);
    }

    @PostMapping("/parse-log")
    public ResponseEntity<String> parseLog(@RequestBody String requestBody) {
        return forwardToPythonServer("/parse-log", requestBody, AiEndpoint.QUICKLOG);
    }

    @PostMapping("/parse-diet")
    public ResponseEntity<String> parseDiet(@RequestBody String requestBody) {
        return forwardToPythonServer("/parse-diet", requestBody, AiEndpoint.DIET);
    }

    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> speechToText(@RequestPart("audio_file") MultipartFile audioFile) {
        try {
            return forwardMultipartToPythonServer("/stt", audioFile);
        } catch (Exception e) {
            return fallback(AiEndpoint.STT, AiFailureClassifier.classify(e));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> aiHealth() {
        String aiStatus = "down";
        String llmStatus = "unknown";
        try {
            ResponseEntity<String> response = aiRestClient.get()
                .uri("/health")
                .retrieve()
                .toEntity(String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                aiStatus = "up";
                String responseBody = response.getBody();
                if (responseBody != null && !responseBody.isBlank()) {
                    try {
                        JsonNode node = objectMapper.readTree(responseBody);
                        if (node.has("llm")) {
                            llmStatus = node.get("llm").asText("unknown");
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            log.warn(
                "event=ai_health_check endpoint=health reason_category={} fallback_used=false",
                AiFailureClassifier.classify(e).name()
            );
        }
        boolean fallbackEffective = isProdEnv() || fallbackEnabled;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("backend", "up");
        body.put("ai", aiStatus);
        body.put("llm", llmStatus);
        body.put("aiBaseUrlConfigured", aiServerUrl != null && !aiServerUrl.isBlank());
        body.put("aiBaseUrlMode", aiBaseUrlMode());
        body.put("aiBaseUrlSanitized", sanitizeAiRootUrl());
        body.put("aiEffectiveBasePath", "/api/ai");
        body.put("fallbackEnabled", fallbackEnabled);
        body.put("fallbackConfigured", fallbackEnabled);
        body.put("fallbackEffective", fallbackEffective);
        body.put("fallbackPolicy", isProdEnv() ? "always_on_for_safety" : "configured");
        return ResponseEntity.ok(body);
    }

    private boolean isProdEnv() {
        String normalized = appEnv == null ? "" : appEnv.trim().toLowerCase();
        return normalized.equals("prod") || normalized.equals("production");
    }

    private String aiBaseUrlMode() {
        String root = configuredAiRootUrl();
        if (root == null || root.isBlank()) {
            return "not_configured";
        }
        return root.matches("(?i).*/api/ai/?$") ? "misconfigured_contains_api_ai" : "root_plus_api_ai";
    }

    private String sanitizeAiRootUrl() {
        String root = configuredAiRootUrl();
        if (root == null || root.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(root);
            String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
            String host = sanitizeHost(uri.getHost());
            int port = uri.getPort();
            return scheme + "://" + host + (port > -1 ? ":" + port : "");
        } catch (Exception e) {
            return "<AI_BASE_URL_CONFIGURED>";
        }
    }

    private String configuredAiRootUrl() {
        if (aiServerUrl == null || aiServerUrl.isBlank()) {
            return "";
        }
        return aiServerUrl.trim().replaceFirst("(?i)/api/ai/?$", "");
    }

    private String sanitizeHost(String host) {
        if (host == null || host.isBlank()) {
            return "<AI_HOST>";
        }
        String normalized = host.trim().toLowerCase();
        if (normalized.equals("localhost") || normalized.equals("127.0.0.1")) {
            return normalized;
        }
        if (normalized.matches("100\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            return "<PC_TAILSCALE_IP>";
        }
        return "<AI_HOST>";
    }

    private ResponseEntity<String> forwardToPythonServer(String path, String body, AiEndpoint endpoint) {
        long startedAt = System.currentTimeMillis();
        if (body == null || body.isBlank()) {
            return fallback(endpoint, StatusReasonCode.ai_bad_response, startedAt);
        }
        try {
            String outboundBody = body;
            boolean parsedJson = false;
            try {
                outboundBody = objectMapper.writeValueAsString(objectMapper.readTree(body));
                parsedJson = true;
            } catch (Exception ignored) {
                // Keep the existing raw forwarding path for malformed payloads so fallback classification is unchanged.
            }

            ResponseEntity<String> response = aiRestClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(outboundBody)
                .retrieve()
                .toEntity(String.class);
            log.info(
                "event=ai_proxy_forward endpoint={} method=POST request_body_present=true request_body_length={} parsed_json={} upstream_path={} upstream_status={} elapsed_ms={}",
                endpoint.logName,
                body.length(),
                parsedJson,
                path,
                response.getStatusCode().value(),
                Math.max(0, System.currentTimeMillis() - startedAt)
            );

            String responseBody = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || responseBody == null || responseBody.isBlank()) {
                return fallback(endpoint, StatusReasonCode.ai_bad_response, startedAt);
            }
            if (!isValidEndpointResponse(endpoint, responseBody)) {
                return fallback(endpoint, StatusReasonCode.ai_schema_mismatch, startedAt);
            }

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody);
        } catch (Exception e) {
            return fallback(endpoint, AiFailureClassifier.classify(e), startedAt);
        }
    }

    private ResponseEntity<String> forwardMultipartToPythonServer(String path, MultipartFile audioFile) throws Exception {
        ByteArrayResource audioResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("audio_file", audioResource);

        ResponseEntity<String> response = aiRestClient.post()
            .uri(path)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(multipartBody)
            .retrieve()
            .toEntity(String.class);

        String body = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful()
            || body == null
            || body.isBlank()
            || !isValidEndpointResponse(AiEndpoint.STT, body)) {
            return fallback(AiEndpoint.STT, StatusReasonCode.ai_bad_response);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }

    private boolean isValidEndpointResponse(AiEndpoint endpoint, String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            return switch (endpoint) {
                case QUICKLOG -> root.has("diet_logs") && root.has("workout_logs") && root.has("overall_summary");
                case DIET -> root.has("items");
                case SUPPLEMENT -> root.has("answer");
                case STT -> root.has("text") && root.has("status");
                case ROUTINE -> root.has("generationStatus") && root.has("routineBlocks");
            };
        } catch (Exception e) {
            return false;
        }
    }

    private ResponseEntity<String> fallback(AiEndpoint endpoint, StatusReasonCode reason) {
        return fallback(endpoint, reason, System.currentTimeMillis());
    }

    private ResponseEntity<String> fallback(AiEndpoint endpoint, StatusReasonCode reason, long startedAt) {
        long elapsedMs = Math.max(0, System.currentTimeMillis() - startedAt);
        log.warn(
            "event=ai_fallback endpoint={} reason_category={} elapsed_ms={} fallback_used=true",
            endpoint.logName,
            reason.name(),
            elapsedMs
        );

        String body = switch (endpoint) {
            case QUICKLOG -> fallbackResponseFactory.quicklog(reason);
            case DIET -> fallbackResponseFactory.diet(reason);
            case SUPPLEMENT -> fallbackResponseFactory.supplement(reason);
            case STT -> fallbackResponseFactory.stt(reason);
            case ROUTINE -> fallbackResponseFactory.routine(reason);
        };

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }

    private enum AiEndpoint {
        ROUTINE("generate-routine"),
        QUICKLOG("parse-log"),
        DIET("parse-diet"),
        SUPPLEMENT("supplement-chat"),
        STT("stt");

        private final String logName;

        AiEndpoint(String logName) {
            this.logName = logName;
        }
    }
}
