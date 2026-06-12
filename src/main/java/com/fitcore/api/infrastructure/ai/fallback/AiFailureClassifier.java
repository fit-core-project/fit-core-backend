package com.fitcore.api.infrastructure.ai.fallback;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;

public final class AiFailureClassifier {
    private AiFailureClassifier() {
    }

    public static StatusReasonCode classify(Throwable throwable) {
        Throwable root = rootCause(throwable);
        String message = safeMessage(throwable) + " " + safeMessage(root);

        if (root instanceof ConnectException || message.contains("connection refused")) {
            return StatusReasonCode.ai_connection_refused;
        }
        if (root instanceof SocketTimeoutException
            || message.contains("timeout")
            || message.contains("timed out")) {
            return StatusReasonCode.ai_timeout;
        }
        if (root instanceof UnknownHostException) {
            return StatusReasonCode.ai_server_unavailable;
        }
        if (throwable instanceof HttpServerErrorException || throwable instanceof HttpClientErrorException) {
            return StatusReasonCode.ai_remote_error;
        }
        if (throwable instanceof RestClientResponseException) {
            return StatusReasonCode.ai_remote_error;
        }
        if (throwable instanceof HttpMessageConversionException || root instanceof JsonProcessingException) {
            return StatusReasonCode.ai_schema_mismatch;
        }
        if (throwable instanceof ResourceAccessException) {
            return StatusReasonCode.ai_server_unavailable;
        }
        if (message.contains("json")
            || message.contains("deserialize")
            || message.contains("schema")
            || message.contains("parse")) {
            return StatusReasonCode.ai_schema_mismatch;
        }
        return StatusReasonCode.unknown_ai_error;
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current == null ? throwable : current;
    }

    private static String safeMessage(Throwable throwable) {
        return throwable == null || throwable.getMessage() == null
            ? ""
            : throwable.getMessage().toLowerCase();
    }
}
