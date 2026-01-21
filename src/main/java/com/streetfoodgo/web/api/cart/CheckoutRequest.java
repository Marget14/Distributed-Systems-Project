package com.streetfoodgo.web.api.cart;

public record CheckoutRequest(
        Long deliveryAddressId,
        String orderType,
        String paymentMethod,
        String specialInstructions
) {}
