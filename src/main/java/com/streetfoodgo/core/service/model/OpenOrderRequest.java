package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OpenOrderRequest(
    @NotNull @Positive Long customerId,
    @NotNull @Positive Long waiterId,
    @NotNull @NotBlank @Size(max = 255) String subject,
    @NotNull @NotBlank @Size(max = 1000) String customerContent
) {}
