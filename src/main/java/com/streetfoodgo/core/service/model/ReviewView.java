package com.streetfoodgo.core.service.model;

import java.time.Instant;

/**
 * View model for Review.
 */
public record ReviewView(
        Long id,
        Long storeId,
        String storeName,
        Long customerId,
        String customerName,
        Long orderId,
        Integer rating,
        Integer foodRating,
        Integer deliveryRating,
        String comment,
        String ownerResponse,
        Instant responseAt,
        Boolean isVerified,
        Instant createdAt
) {
}
