package com.fitcore.api.infrastructure.ai.fallback;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

@Component
public class AiFallbackResponseFactory {
    private static final String AI_DOWN_MESSAGE =
        "\u0041\u0049 \uc11c\ubc84\uac00 \ud604\uc7ac \ub2eb\ud600 \uc788\uc5b4 \uae30\ubcf8 \uc751\ub2f5\uc73c\ub85c \ub300\uccb4\ud588\uc2b5\ub2c8\ub2e4.";

    private final ObjectMapper objectMapper;

    public AiFallbackResponseFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String diet(StatusReasonCode reason) {
        return toJson(Map.of(
            "items", List.of(),
            "status", "fallback",
            "fallback_reason", reason.name()
        ));
    }

    public String quicklog(StatusReasonCode reason) {
        return toJson(Map.of(
            "diet_logs", List.of(),
            "overall_summary", "\u0041\u0049 \uc11c\ubc84\uac00 \ub2eb\ud600 \uc788\uc5b4 \uae30\ubcf8 \ud30c\uc2f1\uc73c\ub85c \ucc98\ub9ac\ud588\uc2b5\ub2c8\ub2e4.",
            "status", "fallback",
            "fallback_reason", reason.name()
        ));
    }

    public String supplement(StatusReasonCode reason) {
        return toJson(Map.of(
            "answer", "\u0041\u0049 \uc11c\ubc84\uac00 \ud604\uc7ac \ub2eb\ud600 \uc788\uc5b4 \uc77c\ubc18 \uc548\uc804 \uc548\ub0b4\ub85c \ub300\uccb4\ud588\uc2b5\ub2c8\ub2e4. \ubcf5\uc6a9 \uc911\uc778 \uc57d, \uc9c8\ud658, \uc784\uc2e0 \uc5ec\ubd80\uac00 \uc788\ub2e4\uba74 \uc758\uc0ac \ub610\ub294 \uc57d\uc0ac\uc640 \uc0c1\ub2f4\ud558\uc138\uc694.",
            "sources", List.of(),
            "mode", "fallback",
            "fallback_reason", reason.name()
        ));
    }

    public String stt(StatusReasonCode reason) {
        return toJson(Map.of(
            "text", "",
            "status", "unavailable",
            "message", "\u0041\u0049 \uc74c\uc131 \uc778\uc2dd \uc11c\ubc84\uac00 \ud604\uc7ac \ub2eb\ud600 \uc788\uc5b4 \ud14d\uc2a4\ud2b8 \uc785\ub825\uc744 \uc0ac\uc6a9\ud574 \uc8fc\uc138\uc694.",
            "fallbackReason", reason.name()
        ));
    }

    public String routine(StatusReasonCode reason) {
        return toJson(Map.of(
            "routineDraftId", "fallback-ai-proxy",
            "generationStatus", "fallback",
            "statusReasonCode", reason.name(),
            "isFallback", true,
            "totalEstimatedTime", 30,
            "summaryTitle", "\uae30\ubcf8 \uc6b4\ub3d9 \ub8e8\ud2f4",
            "rationaleSummary", List.of(AI_DOWN_MESSAGE),
            "warnings", List.of("\u0041\u0049 \uc11c\ubc84 \uc5f0\uacb0 \uc2e4\ud328\ub85c \uac1c\uc778\ud654 \uc815\ud655\ub3c4\uac00 \uc81c\ud55c\ub429\ub2c8\ub2e4."),
            "routineBlocks", List.of()
        ));
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{\"status\":\"fallback\",\"message\":\"" + AI_DOWN_MESSAGE + "\"}";
        }
    }
}
