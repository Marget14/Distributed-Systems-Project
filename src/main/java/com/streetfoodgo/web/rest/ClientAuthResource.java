package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.security.ClientDetails;
import com.streetfoodgo.core.security.ClientDetailsService;
import com.streetfoodgo.core.security.JwtService;
import com.streetfoodgo.web.rest.model.ClientTokenRequest;
import com.streetfoodgo.web.rest.model.ClientTokenResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for client authentication (JWT tokens).
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientAuthResource {

    private final ClientDetailsService clientDetailsService;
    private final JwtService jwtService;

    public ClientAuthResource(
            final ClientDetailsService clientDetailsService,
            final JwtService jwtService) {

        if (clientDetailsService == null) throw new NullPointerException();
        if (jwtService == null) throw new NullPointerException();

        this.clientDetailsService = clientDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/client-tokens")
    public ClientTokenResponse clientToken(@RequestBody @Valid ClientTokenRequest request) {
        final String clientId = request.clientId();
        final String clientSecret = request.clientSecret();

        // Authenticate client
        final ClientDetails client = this.clientDetailsService
                .authenticate(clientId, clientSecret)
                .orElse(null);

        if (client == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid client credentials");
        }

        // Issue JWT token
        final String token = this.jwtService.issue("client:" + client.id(), client.roles());

        return new ClientTokenResponse(token, "Bearer", 3600);
    }
}