package com.fitcore.api.global.common.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import com.fitcore.api.global.error.ErrorCode;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;

    // ErrorCode를 받아 에러 응답 객체를 생성하는 정적 메서드
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(errorCode.getStatus().value())
            .error(errorCode.getStatus().name())
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .build();
    }

    // 유효성 검사(Validation) 에러 등을 위한 확장 메서드
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(errorCode.getStatus().value())
            .error(errorCode.getStatus().name())
            .code(errorCode.getCode())
            .message(message)
            .build();
    }
}
