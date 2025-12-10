package com.streetfoodgo.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseEntityHelper {

    public static ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("timestamp", Instant.now());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<Map<String, Object>> created(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("timestamp", Instant.now());
        response.put("data", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static ResponseEntity<Map<String, Object>> validationErrors(BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("timestamp", Instant.now());
        response.put("message", "Validation failed");

        List<String> errors = bindingResult.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        response.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
