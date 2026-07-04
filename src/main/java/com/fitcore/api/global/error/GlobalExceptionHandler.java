package com.fitcore.api.global.error;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import com.fitcore.api.domain.program.exception.ProgramException;
import com.fitcore.api.global.common.response.ErrorResponse;
import com.fitcore.api.global.error.exception.BusinessException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. 우리가 직접 만든 BusinessException 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getErrorCode().getMessage());
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("ValidationException: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .distinct()
            .collect(Collectors.joining(", "));
        ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            message.isBlank() ? ErrorCode.INVALID_INPUT_VALUE.getMessage() : message
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ProgramException.class)
    protected ResponseEntity<ErrorResponse> handleProgramException(ProgramException e) {
        log.error("ProgramException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
            .status(e.getStatus().value())
            .error(e.getStatus().name())
            .code(e.getCode())
            .message(e.getMessage())
            .build();
        return ResponseEntity.status(e.getStatus()).body(response);
    }

    // 2. 일반적인 모든 예외(Exception) 처리 (최후의 보루)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);

        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
            .code("SERVER_ERROR")
            .message("예상치 못한 오류가 발생했습니다.")
            .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
