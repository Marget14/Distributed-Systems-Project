package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for updating store information.
 */
public record UpdateStoreRequest(
        @NotNull @NotBlank @Size(min = 3, max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull @Size(max = 500) String openingHours,
        @NotNull Boolean isOpen,
        @NotNull @Positive BigDecimal minimumOrderAmount,
        @NotNull Boolean acceptsDelivery,
        @NotNull Boolean acceptsPickup,
        @Positive BigDecimal deliveryFee,
        @Positive Integer estimatedDeliveryTimeMinutes,
        @Size(max = 500) String imageUrl
) {}