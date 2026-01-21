package com.streetfoodgo.core.service.model;

/**
 * Statistics about orders for a customer or store.
 */
public record OrderStatistics(
        Long totalOrders,
        Long completedOrders,
        Long cancelledOrders,
        Long pendingOrders
) {
}
