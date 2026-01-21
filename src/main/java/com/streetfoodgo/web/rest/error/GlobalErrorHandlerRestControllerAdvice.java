package com.streetfoodgo.web.rest.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global error handler for REST API endpoints.
 * Returns JSON error responses with proper HTTP status codes and structured error information.
 */
@RestControllerAdvice(basePackages = "com.streetfoodgo.web.rest")
@Order(1)
public class GlobalErrorHandlerRestControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandlerRestControllerAdvice.class);

    /**
     * Handle validation errors on @RequestBody with @Valid annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest httpServletRequest) {

        LOGGER.warn("Validation error [POST/PUT {}]: {}",
                httpServletRequest.getRequestURI(),
                exception.getBindingResult().getAllErrors().size() + " errors"
        );

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Error");
        errorResponse.put("message", "Request body validation failed");
        errorResponse.put("path", httpServletRequest.getRequestURI());

        Map<String, String> fieldErrors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        if (!fieldErrors.isEmpty()) {
            errorResponse.put("fieldErrors", fieldErrors);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle constraint violations in query parameters and path variables.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            final ConstraintViolationException exception,
            final HttpServletRequest httpServletRequest) {

        LOGGER.warn("Constraint violation [{}]: {}",
                httpServletRequest.getRequestURI(),
                exception.getMessage()
        );

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Constraint Violation");
        errorResponse.put("message", "Validation constraint violated");
        errorResponse.put("path", httpServletRequest.getRequestURI());

        Map<String, String> violations = exception.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        cv -> cv.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        if (!violations.isEmpty()) {
            errorResponse.put("violations", violations);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAnyError(
            final Exception exception,
            final HttpServletRequest httpServletRequest) {

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (exception instanceof NoResourceFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (exception instanceof SecurityException) {
            httpStatus = HttpStatus.UNAUTHORIZED;
        } else if (exception instanceof AuthorizationDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN;
        } else if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
            httpStatus = HttpStatus.BAD_REQUEST;
        } else if (exception instanceof ResponseStatusException responseStatusException) {
            try {
                httpStatus = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            } catch (Exception ignored) {}
        }

        LOGGER.warn("REST API error [{} {}] -> status={} cause={}: {}",
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI(),
                httpStatus.value(),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception
        );

        final ApiError apiError = new ApiError(
                Instant.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                exception.getMessage() != null ? exception.getMessage() : "An error occurred",
                httpServletRequest.getRequestURI()
        );

        return ResponseEntity.status(httpStatus).body(apiError);
    }
}