package com.streetfoodgo.core.model;

/**
 * Status lifecycle of an order.
 */
public enum OrderStatus {
    /**
     * Order placed by customer, awaiting store acceptance.
     */
    PENDING,

    /**
     * Store accepted the order and is preparing it.
     */
    PREPARING,

    /**
     * Order is ready for pickup or out for delivery.
     */
    READY,

    /**
     * Order is being delivered (only for DELIVERY orders).
     */
    DELIVERING,

    /**
     * Order completed successfully.
     */
    COMPLETED,

    /**
     * Order was rejected by store.
     */
    REJECTED,

    /**
     * Order was cancelled by customer.
     */
    CANCELLED
}