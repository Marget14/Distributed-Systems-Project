package com.streetfoodgo.core.service.model;

public record UpdateMenuItemIngredientRequest(
        String name,
        String description,
        Boolean isRemovable,
        Boolean isAllergen,
        String allergenInfo,
        Integer displayOrder
) {}
