package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.MenuCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * View/DTO for MenuItem entity.
 */
public record MenuItemView(
        Long id,
        Long storeId,
        String storeName,
        String name,
        String description,
        BigDecimal price,
        MenuCategory category,
        Boolean available,
        String imageUrl,
        Instant createdAt,
        List<MenuItemOptionView> options,
        List<MenuItemIngredientView> ingredients
) {}