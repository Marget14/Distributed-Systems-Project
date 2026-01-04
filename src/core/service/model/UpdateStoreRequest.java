package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for updating store information.
 */
public record UpdateStoreRequest(
        @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @Size(max = 500) String openingHours,
        Boolean isOpen,
        BigDecimal minimumOrderAmount,
        Boolean acceptsDelivery,
        Boolean acceptsPickup,
        BigDecimal deliveryFee,
        Integer estimatedDeliveryTimeMinutes,
        @Size(max = 500) String imageUrl
) {}