package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.security.JwtService;
import com.streetfoodgo.web.rest.model.UserLoginRequest;
import com.streetfoodgo.web.rest.model.UserTokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for user authentication (JWT tokens for customers/owners/admins).
 * Issues JWT tokens used for API requests with stateless authentication.
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "APIs for user authentication and JWT token management")
public class UserAuthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthResource.class);

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserAuthResource(
            final PersonRepository personRepository,
            final PasswordEncoder passwordEncoder,
            final JwtService jwtService) {
        if (personRepository == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        if (jwtService == null) throw new NullPointerException();
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Authenticate user and issue a JWT token.
     * Token is used for subsequent API requests via Authorization: Bearer <token> header.
     */
    @PostMapping("/login")
    @Operation(summary = "User login",
               description = "Authenticate with email and password to receive a JWT token. " +
                            "Use the returned token in the Authorization header (Bearer <token>) for subsequent API requests.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token issued",
                     content = @Content(mediaType = "application/json",
                                       schema = @Schema(implementation = UserTokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request format or missing required fields"),
        @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public UserTokenResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials (email and password)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserLoginRequest.class)))
            @RequestBody @Valid final UserLoginRequest request) {

        LOGGER.info("Login attempt for email: {}", request.email());

        final Person person = this.personRepository
                .findByEmailAddressIgnoreCase(request.email())
                .orElseThrow(() -> {
                    LOGGER.warn("Login failed: user not found for email: {}", request.email());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!this.passwordEncoder.matches(request.password(), person.getPasswordHash())) {
            LOGGER.warn("Login failed: invalid password for email: {}", request.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        final String role = person.getType().name(); // CUSTOMER / OWNER / ADMIN
        final List<String> roles = List.of(role);
        final String token = this.jwtService.issue("user:" + person.getId(), roles);

        LOGGER.info("Login successful for email: {} with role: {}", request.email(), role);

        return new UserTokenResponse(
                token,
                "Bearer",
                3600,
                person.getId(),
                person.getEmailAddress(),
                person.getType().name(),
                roles
        );
    }
}
