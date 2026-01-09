package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for updating order status.
 */
public record UpdateOrderStatusRequest(
        @NotNull @Positive Long orderId,
        @NotNull OrderStatus newStatus
) {}