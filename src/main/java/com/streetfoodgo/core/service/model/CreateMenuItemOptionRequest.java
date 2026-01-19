package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMenuItemOptionRequest(
        @NotNull Long menuItemId,
        @NotNull @NotBlank String name,
        String description,
        @NotNull Boolean isRequired,
        @NotNull Boolean allowMultiple,
        Integer minSelections,
        Integer maxSelections,
        Integer displayOrder
) {}
