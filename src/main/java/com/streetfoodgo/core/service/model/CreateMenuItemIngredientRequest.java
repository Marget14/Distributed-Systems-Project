package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMenuItemIngredientRequest(
        @NotNull @NotBlank String name,
        String description,
        @NotNull Boolean isRemovable,
        @NotNull Boolean isAllergen,
        String allergenInfo,
        Integer displayOrder
) {}
