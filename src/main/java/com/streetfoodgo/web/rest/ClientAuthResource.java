package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.security.ClientDetailsService;
import com.streetfoodgo.core.security.ClientDetails;
import com.streetfoodgo.core.security.JwtService;

import com.streetfoodgo.web.rest.model.ClientTokenRequest;
import com.streetfoodgo.web.rest.model.ClientTokenResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication.
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientAuthResource {

    private final ClientDetailsService clientDetailsService;
    private final JwtService jwtService;

    public ClientAuthResource(final ClientDetailsService clientDetailsService, final JwtService jwtService) {
        if (clientDetailsService == null) throw new NullPointerException();
        if (jwtService == null) throw new NullPointerException();
        this.clientDetailsService = clientDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/client-tokens")
    @RateLimit(value = 5, duration = 60)
    public ResponseEntity<?> clientToken(@RequestBody @Valid ClientTokenRequest clientTokenRequest) {
        final String clientId = clientTokenRequest.clientId();
        final String clientSecret = clientTokenRequest.clientSecret();

        final ClientDetails client = this.clientDetailsService.authenticate(clientId, clientSecret).orElse(null);
        if (client == null) {
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("error", "invalid_client");
            errorDetails.put("error_description", "Invalid client credentials");
            errorDetails.put("timestamp", Instant.now().toString());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
        }

        final String token = this.jwtService.issue("client:" + client.id(), client.roles());
        return ResponseEntity.ok(new ClientTokenResponse(token, "Bearer", 60 * 60));
    }
}
