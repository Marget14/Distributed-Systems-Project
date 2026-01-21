package com.streetfoodgo.web.ui.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global error handler for UI controllers (returns HTML error pages).
 * Handles validation errors, not found errors, and general exceptions.
 */
@ControllerAdvice(basePackages = "com.streetfoodgo.web.ui")
@Order(2)
public class GlobalErrorHandlerControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandlerControllerAdvice.class);

    /**
     * Handle validation errors on form submissions.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Model model) {

        LOGGER.warn("Form validation failed [{}]: {} field errors",
                httpServletRequest.getRequestURI(),
                exception.getBindingResult().getFieldErrorCount()
        );

        httpServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        model.addAttribute("message", "Validation failed. Please check your input.");
        model.addAttribute("path", httpServletRequest.getRequestURI());
        model.addAttribute("errors", exception.getBindingResult().getFieldErrors());

        return "error/error";
    }

    /**
     * Handle 404 Not Found errors.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(
            final NoResourceFoundException exception,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Model model) {

        LOGGER.warn("Resource not found: {}", httpServletRequest.getRequestURI());

        httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("message", "The requested resource was not found.");
        model.addAttribute("path", httpServletRequest.getRequestURI());

        return "error/404";
    }

    /**
     * Handle security and authorization exceptions.
     */
    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(
            final SecurityException exception,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Model model) {

        LOGGER.warn("Security error [{}]: {}", httpServletRequest.getRequestURI(), exception.getMessage());

        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        model.addAttribute("message", "Authentication required. Please log in.");
        model.addAttribute("path", httpServletRequest.getRequestURI());

        return "auth/login";
    }

    /**
     * Handle access denied / forbidden errors.
     */
    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public String handleAccessDenied(
            final org.springframework.security.authorization.AuthorizationDeniedException exception,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Model model) {

        LOGGER.warn("Access denied [{}]: User lacks required permissions", httpServletRequest.getRequestURI());

        httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
        model.addAttribute("message", "You do not have permission to access this resource.");
        model.addAttribute("path", httpServletRequest.getRequestURI());

        return "error/403";
    }

    /**
     * Handle ResponseStatusException.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(
            final ResponseStatusException exception,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Model model) {

        int statusCode = exception.getStatusCode().value();
        LOGGER.warn("Response status error [{}]: {} {}",
                httpServletRequest.getRequestURI(),
                statusCode,
                exception.getReason()
        );

        httpServletResponse.setStatus(statusCode);
        model.addAttribute("message", exception.getReason() != null ? exception.getReason() : "An error occurred");
        model.addAttribute("path", httpServletRequest.getRequestURI());

        if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 403) {
            return "error/403";
        } else if (statusCode >= 500) {
            return "error/500";
        }

        return "error/error";
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public String handleAnyError(
            final Exception exception,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final Model model) {

        LOGGER.error("Unexpected error [{}]: {}",
                httpServletRequest.getRequestURI(),
                exception.getClass().getSimpleName(),
                exception
        );

        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("message", "An unexpected error occurred. Please try again later.");
        model.addAttribute("path", httpServletRequest.getRequestURI());
        model.addAttribute("exception", exception.getClass().getSimpleName());

        return "error/500";
    }
}