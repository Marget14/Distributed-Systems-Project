package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for accepting an order.
 */
public record AcceptOrderRequest(
        @NotNull @Positive Long orderId
) {}