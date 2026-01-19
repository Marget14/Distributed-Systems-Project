package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.model.StoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for creating a new store.
 */
public record CreateStoreRequest(
        @NotNull @NotBlank @Size(min = 3, max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull CuisineType cuisineType,
        @NotNull StoreType storeType,
        @NotNull @NotBlank @Size(min = 5, max = 500) String address,
        Double latitude,
        Double longitude,
        @NotNull @Size(max = 100) String area,
        @Size(max = 500) String openingHours,
        @NotNull @Positive BigDecimal minimumOrderAmount,
        @NotNull Boolean acceptsDelivery,
        @NotNull Boolean acceptsPickup,
        @Positive BigDecimal deliveryFee,
        @Positive Integer estimatedDeliveryTimeMinutes,
        @Size(max = 500) String imageUrl
) {}