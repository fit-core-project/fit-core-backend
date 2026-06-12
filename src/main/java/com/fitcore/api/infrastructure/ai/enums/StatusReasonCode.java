package com.fitcore.api.infrastructure.ai.enums;

public enum StatusReasonCode {
    none,
    llmTimeout,
    schemaError,
    networkError,
    emptyCandidate,
    ai_server_unavailable,
    ai_timeout,
    ai_connection_refused,
    ai_bad_response,
    ai_schema_mismatch,
    ai_remote_error,
    ai_disabled,
    unknown_ai_error
}
