package com.streetfoodgo.core.service.model;

import java.math.BigDecimal;

/**
 * View/DTO for OrderItem entity.
 */
public record OrderItemView(
        Long id,
        MenuItemView menuItem,
        Integer quantity,
        BigDecimal priceAtOrder,
        String specialInstructions
) {
    public BigDecimal subtotal() {
        return priceAtOrder.multiply(BigDecimal.valueOf(quantity));
    }
}