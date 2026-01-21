package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.MenuCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for creating a menu item.
 */
public record CreateMenuItemRequest(
        @NotNull @Positive Long storeId,
        @NotNull @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull @Positive BigDecimal price,
        @NotNull MenuCategory category,
        @Size(max = 500) String imageUrl
) {}