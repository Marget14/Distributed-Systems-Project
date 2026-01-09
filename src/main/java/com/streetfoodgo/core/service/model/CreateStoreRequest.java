package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.model.StoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for creating a new store.
 */
public record CreateStoreRequest(
        @NotNull @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull CuisineType cuisineType,
        @NotNull StoreType storeType,
        @NotNull @NotBlank @Size(max = 500) String address,
        Double latitude,
        Double longitude,
        @Size(max = 100) String area,
        @Size(max = 500) String openingHours,
        BigDecimal minimumOrderAmount,
        Boolean acceptsDelivery,
        Boolean acceptsPickup,
        BigDecimal deliveryFee,
        Integer estimatedDeliveryTimeMinutes,
        @Size(max = 500) String imageUrl
) {}