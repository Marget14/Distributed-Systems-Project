package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.model.StoreType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * View/DTO for Store entity.
 */
public record StoreView(
        Long id,
        PersonView owner,
        String name,
        String description,
        CuisineType cuisineType,
        StoreType storeType,
        String address,
        Double latitude,
        Double longitude,
        String area,
        String openingHours,
        Boolean isOpen,
        BigDecimal minimumOrderAmount,
        Boolean acceptsDelivery,
        Boolean acceptsPickup,
        BigDecimal deliveryFee,
        Integer estimatedDeliveryTimeMinutes,
        String imageUrl,
        Instant createdAt
) {}