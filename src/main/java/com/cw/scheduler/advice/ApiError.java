package com.cw.scheduler.advice;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ApiError {
    private final boolean success = false;
    private final String message;
    private final HttpStatus status;
    private final String path;
    private final ErrorCode errorCode;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final Map<String, String> validationErrors;

    // Constructor without validation errors
    public ApiError(String message, HttpStatus status, String path, ErrorCode errorCode) {
        this(message, status, path, errorCode, null);
    }
}