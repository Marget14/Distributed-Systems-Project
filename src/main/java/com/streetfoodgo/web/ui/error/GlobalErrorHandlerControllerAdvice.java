package com.streetfoodgo.web.ui.error;

import com.streetfoodgo.web.rest.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

/**
 * Provides global error handling and custom error templates.
 */
@ControllerAdvice(basePackages = "com.streetfoodgo.web.ui")
@Order(2)
public class GlobalErrorHandlerControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandlerControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAnyError(final Exception exception,
                                                   final HttpServletRequest httpServletRequest) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        // Προσθήκη περισσότερων exception types
        if (exception instanceof NoResourceFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (exception instanceof SecurityException) {
            httpStatus = HttpStatus.UNAUTHORIZED;
        } else if (exception instanceof AuthorizationDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN;
        } else if (exception instanceof ResponseStatusException responseStatusException) {
            try {
                httpStatus = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            } catch (Exception ignored) {}
        } else if (exception instanceof jakarta.validation.ConstraintViolationException) {
            httpStatus = HttpStatus.BAD_REQUEST;
        } else if (exception instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        if (httpStatus.is5xxServerError()) {
            LOGGER.error("REST error [{} {}] -> status={} cause={}: {}",
                    httpServletRequest.getMethod(),
                    httpServletRequest.getRequestURI(),
                    httpStatus.value(),
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception // Include stack trace for server errors
            );
        } else {
            LOGGER.warn("REST error [{} {}] -> status={} cause={}: {}",
                    httpServletRequest.getMethod(),
                    httpServletRequest.getRequestURI(),
                    httpStatus.value(),
                    exception.getClass().getSimpleName(),
                    exception.getMessage()
            );
        }

        String message = exception.getMessage();
        if (httpStatus.is4xxClientError() && message == null) {
            message = httpStatus.getReasonPhrase();
        } else if (httpStatus.is5xxServerError()) {
            message = "Internal server error";
        }

        final ApiError apiError = new ApiError(
                Instant.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                httpServletRequest.getRequestURI()
        );

        return ResponseEntity.status(httpStatus).body(apiError);
    }
}
