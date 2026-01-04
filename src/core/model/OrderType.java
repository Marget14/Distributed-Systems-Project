package com.streetfoodgo.core.model;

/**
 * Type of order fulfillment.
 */
public enum OrderType {
    /**
     * Customer will pick up the order from the store.
     */
    PICKUP,

    /**
     * Order will be delivered to customer's address.
     */
    DELIVERY
}