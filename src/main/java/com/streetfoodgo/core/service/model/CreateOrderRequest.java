package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.OrderType;
import com.streetfoodgo.core.model.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for creating an order.
 */
public record CreateOrderRequest(
        @NotNull @Positive Long customerId,
        @NotNull @Positive Long storeId,
        Long deliveryAddressId,
        @NotNull OrderType orderType,
        @NotNull PaymentMethod paymentMethod,
        String paymentTransactionId,
        @NotNull @NotEmpty List<OrderItemRequest> items,
        @Size(max = 1000) String customerNotes
) {}