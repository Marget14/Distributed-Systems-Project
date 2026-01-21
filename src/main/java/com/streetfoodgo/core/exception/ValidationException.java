package com.streetfoodgo.core.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception for validation errors.
 */
public class ValidationException extends BusinessException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void addFieldError(String field, String error) {
        fieldErrors.put(field, error);
    }
}
