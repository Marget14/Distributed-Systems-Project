package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for updating a menu item.
 */
public record UpdateMenuItemRequest(
        @NotNull @NotBlank @Size(min = 2, max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull @Positive BigDecimal price,
        @NotNull Boolean available,
        @Size(max = 500) String imageUrl
) {}