package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.security.JwtService;
import com.streetfoodgo.core.service.UserService;
import com.streetfoodgo.web.rest.model.UserLoginRequest;
import com.streetfoodgo.web.rest.model.UserLoginResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserAuthResource {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public UserAuthResource(AuthenticationManager authenticationManager,
                            JwtService jwtService,
                            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.issue(userDetails.getUsername(), userDetails.getAuthorities());

            // Get user info
            UserInfo userInfo = userService.getUserInfo(request.email());

            return ResponseEntity.ok(new UserLoginResponse(
                    token,
                    "Bearer",
                    3600,
                    userInfo
            ));

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }
}

// DTOs για το login
record UserLoginRequest(String email, String password) {}
record UserLoginResponse(String accessToken, String tokenType, long expiresIn, UserInfo userInfo) {}
record UserInfo(String id, String email, String firstName, String lastName) {}
