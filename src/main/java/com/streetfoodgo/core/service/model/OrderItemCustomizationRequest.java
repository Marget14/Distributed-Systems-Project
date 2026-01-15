package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotNull;

/**
 * Request to add a customization to an order item.
 */
public record OrderItemCustomizationRequest(
        @NotNull Long menuItemChoiceId
) {
}
