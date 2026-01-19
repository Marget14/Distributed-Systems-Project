package com.streetfoodgo.web.api.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddToCartRequest(
        @NotNull Long menuItemId,
        @Min(1) int quantity,
        List<Long> selectedChoiceIds,
        List<Long> removedIngredientIds,
        String specialInstructions
) {}
