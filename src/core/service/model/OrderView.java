package com.streetfoodgo.core.service.model;

import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.model.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * View/DTO for Order entity.
 */
public record OrderView(
        Long id,
        PersonView customer,
        StoreView store,
        OrderType orderType,
        DeliveryAddressView deliveryAddress,
        List<OrderItemView> items,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal total,
        String customerNotes,
        String rejectionReason,
        Instant createdAt,
        Instant acceptedAt,
        Instant readyAt,
        Instant deliveringAt,
        Instant completedAt,
        Instant rejectedAt,
        Instant cancelledAt
) {
}