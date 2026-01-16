package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for rejecting an order.
 */
public record RejectOrderRequest(
        @NotNull @Positive Long orderId,
        @NotNull @NotBlank @Size(max = 1000) String reason
) {}