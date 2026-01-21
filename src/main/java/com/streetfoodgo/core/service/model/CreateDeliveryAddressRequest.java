package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a delivery address.
 */
public record CreateDeliveryAddressRequest(
        @NotNull @NotBlank @Size(max = 100) String label,
        @NotNull @NotBlank @Size(max = 500) String street,
        @NotNull @NotBlank @Size(max = 100) String city,
        Long number,
        @Size(max = 100) String area,
        @NotNull @NotBlank @Size(max = 20) String postalCode,
        @Size(max = 18) String phoneNumber,
        Double latitude,
        Double longitude
) {}