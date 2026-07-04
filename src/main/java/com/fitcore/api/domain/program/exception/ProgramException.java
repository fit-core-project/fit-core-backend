package com.fitcore.api.domain.program.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ProgramException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ProgramException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ProgramException badRequest(String message) {
        return new ProgramException(HttpStatus.BAD_REQUEST, "PROGRAM_BAD_REQUEST", message);
    }

    public static ProgramException notFound() {
        return new ProgramException(HttpStatus.NOT_FOUND, "PROGRAM_NOT_FOUND", "Program not found.");
    }

    public static ProgramException conflict(String message) {
        return new ProgramException(HttpStatus.CONFLICT, "PROGRAM_CONFLICT", message);
    }
}
