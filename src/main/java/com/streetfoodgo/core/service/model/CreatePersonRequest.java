package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.PersonType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating/registering a new person.
 */
public record CreatePersonRequest(
        @NotNull PersonType type,
        @NotNull @NotBlank @Size(max = 100) String firstName,
        @NotNull @NotBlank @Size(max = 100) String lastName,
        @NotNull @NotBlank @Size(max = 100) @Email String emailAddress,
        @NotNull @NotBlank @Size(max = 18) String mobilePhoneNumber,
        @NotNull @NotBlank @Size(min = 6, max = 50) String rawPassword
) {}