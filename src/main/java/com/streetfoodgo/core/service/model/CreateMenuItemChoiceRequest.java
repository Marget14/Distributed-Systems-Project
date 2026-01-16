package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateMenuItemChoiceRequest(
        @NotNull @NotBlank String name,
        String description,
        @NotNull BigDecimal additionalPrice,
        @NotNull Boolean isAvailable,
        @NotNull Boolean isDefault,
        Integer displayOrder
) {}
