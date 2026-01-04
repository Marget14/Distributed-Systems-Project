package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for updating a menu item.
 */
public record UpdateMenuItemRequest(
        @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @Positive BigDecimal price,
        Boolean available,
        @Size(max = 500) String imageUrl
) {}