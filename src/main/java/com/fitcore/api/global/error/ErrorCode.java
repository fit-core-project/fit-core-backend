package com.fitcore.api.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // 유저 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 유저를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 이메일입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "U003", "접권 권한이 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "U004", "접근이 거부되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "U005", "해당 리소스를 찾을 수 없습니다."),


    ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "해당 루틴를 찾을 수 없습니다."),

    
    EXERCISE_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "해당 운동 정보를 찾을 수 없습니다."),
    // AI 서버 에러
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "AI 서버와의 통신 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
