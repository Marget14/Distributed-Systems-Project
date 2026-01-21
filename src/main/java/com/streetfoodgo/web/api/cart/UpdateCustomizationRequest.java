package com.streetfoodgo.web.api.cart;

import java.util.List;

public record UpdateCustomizationRequest(
        List<Long> selectedChoiceIds,
        List<Long> removedIngredientIds,
        String specialInstructions
) {}
