package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for adding an item to an order.
 */
public record OrderItemRequest(
        @NotNull @Positive Long menuItemId,
        @NotNull @Positive Integer quantity,
        @Size(max = 500) String specialInstructions
) {}