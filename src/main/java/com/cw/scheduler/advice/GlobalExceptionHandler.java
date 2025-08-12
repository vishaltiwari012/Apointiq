package com.cw.scheduler.advice;

import com.cw.scheduler.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Fallback for all unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildErrorResponseEntity(new ApiError(
                "Something went wrong!",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                ErrorCode.SOMETHING_WENT_WRONG
        ));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS,
                request.getRequestURI(),
                ErrorCode.RATE_LIMIT_EXCEEDED
        ));
    }


    // bad credentials exception
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                ErrorCode.BAD_CREDENTIALS
        ));
    }

    // handle multipart exception (e.g., invalid file upload)
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiError> handleMultipartException(MultipartException ex, HttpServletRequest request) {
        log.error("Multipart parsing error: {}", ex.getMessage());
        log.error("Request Content-Type: {}", request.getContentType());
        return buildErrorResponseEntity(new ApiError(
                "Invalid file upload or multipart request",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                ErrorCode.MULTIPART_ERROR
        ));
    }

    // User not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                ErrorCode.USER_NOT_FOUND
        ));
    }

    // Invalid credentials
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        log.warn("Invalid credentials at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                ErrorCode.INVALID_CREDENTIALS
        ));
    }

    // Duplicate resource (e.g., email or phone already exists)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                ErrorCode.DUPLICATE_RESOURCE
        ));
    }

    // OTP expired
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiError> handleOtpExpired(OtpExpiredException ex, HttpServletRequest request) {
        log.warn("OTP expired at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                ErrorCode.OTP_EXPIRED
        ));
    }

    // OTP invalid
    @ExceptionHandler(OtpVerificationException.class)
    public ResponseEntity<ApiError> handleOtpInvalid(OtpVerificationException ex, HttpServletRequest request) {
        log.warn("OTP verification failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                ErrorCode.INVALID_OTP
        ));
    }

    // Appointment not found
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ApiError> handleAppointmentNotFound(AppointmentNotFoundException ex, HttpServletRequest request) {
        log.warn("Appointment not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                ErrorCode.APPOINTMENT_NOT_FOUND
        ));
    }

    // Scheduling conflict
    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<ApiError> handleScheduleConflict(ScheduleConflictException ex, HttpServletRequest request) {
        log.warn("Schedule conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                ErrorCode.SCHEDULE_CONFLICT
        ));
    }

    // Outside Working Hours Exception
    @ExceptionHandler(OutsideWorkingHoursException.class)
    public ResponseEntity<ApiError> handleOutsideWorkingHoursException(OutsideWorkingHoursException ex, HttpServletRequest request) {
        log.warn("Attempted appointment outside working hours at [{}]: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                ErrorCode.OUTSIDE_WORKING_HOURS
        ));
    }


    // Provider unavailable
    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<ApiError> handleProviderUnavailable(ProviderUnavailableException ex, HttpServletRequest request) {
        log.warn("Provider unavailable at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                ErrorCode.PROVIDER_UNAVAILABLE
        ));
    }

    // Email send failure
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiError> handleEmailSend(EmailSendException ex, HttpServletRequest request) {
        log.error("Email sending failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE,
                request.getRequestURI(),
                ErrorCode.EMAIL_SEND_FAILED
        ));
    }

    // Bad request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                ErrorCode.BAD_REQUEST
        ));
    }

    // Access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                "Access is denied.",
                HttpStatus.FORBIDDEN,
                request.getRequestURI(),
                ErrorCode.ACCESS_DENIED
        ));
    }

    // Generic resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                ErrorCode.RESOURCE_NOT_FOUND
        ));
    }

    // Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        log.warn("Validation failed: {}", errors);
        return buildErrorResponseEntity(new ApiError(
                "Validation Failed",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                ErrorCode.VALIDATION_FAILED,
                errors
        ));
    }

    // Invalid Token Exception
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token: {}", ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                ErrorCode.INVALID_TOKEN
        ));
    }

    // Token expired Exception
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiError> handleTokenExpired(TokenExpiredException ex, HttpServletRequest request) {
        log.warn("Token expired: {}", ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.GONE,
                request.getRequestURI(),
                ErrorCode.TOKEN_EXPIRED
        ));
    }

    // Unauthorized Action Exception
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiError> handleUnauthorizedAction(UnauthorizedActionException ex, HttpServletRequest request) {
        log.warn("Unauthorized action: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                request.getRequestURI(),
                ErrorCode.ACCESS_DENIED
        );
        return buildErrorResponseEntity(apiError);
    }

    // TooMany Requests Exception
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return buildErrorResponseEntity(new ApiError(
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS,
                request.getRequestURI(),
                ErrorCode.TOO_MANY_REQUESTS
        ));
    }

    // Role Not Found Exception
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiError> handleRoleNotFoundException(RoleNotFoundException ex, HttpServletRequest request) {
        log.warn("Role not found: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                ErrorCode.ROLE_NOT_FOUND
        );
        return buildErrorResponseEntity(apiError);
    }

    // Reusable method for building ApiError responses
    private ResponseEntity<ApiError> buildErrorResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}

