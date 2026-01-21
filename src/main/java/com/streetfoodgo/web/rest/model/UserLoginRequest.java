package com.streetfoodgo.web.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
        @NotNull @NotBlank String email,
        @NotNull @NotBlank String password
) {}
