package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.security.JwtService;
import com.streetfoodgo.web.rest.model.UserLoginRequest;
import com.streetfoodgo.web.rest.model.UserTokenResponse;

import jakarta.validation.Valid;
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
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserAuthResource {

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

    @PostMapping("/login")
    public UserTokenResponse login(@RequestBody @Valid final UserLoginRequest request) {
        final Person person = this.personRepository
                .findByEmailAddressIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!this.passwordEncoder.matches(request.password(), person.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        final String role = person.getType().name(); // CUSTOMER / OWNER / ADMIN
        final List<String> roles = List.of(role);
        final String token = this.jwtService.issue("user:" + person.getId(), roles);

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
