package com.streetfoodgo.core.model;

/**
 * Types of users in StreetFoodGo platform.
 */
public enum PersonType {
    /**
     * Customer who places orders.
     */
    CUSTOMER,

    /**
     * Store owner who manages store and fulfills orders.
     */
    OWNER,

    /**
     * Platform administrator with full access.
     */
    ADMIN
}