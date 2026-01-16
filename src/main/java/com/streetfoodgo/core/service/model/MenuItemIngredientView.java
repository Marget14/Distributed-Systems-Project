package com.streetfoodgo.core.service.model;

/**
 * View model for menu item ingredient.
 */
public record MenuItemIngredientView(
        Long id,
        String name,
        String description,
        Boolean isRemovable,
        Boolean isAllergen,
        String allergenInfo,
        Integer displayOrder
) {
}
