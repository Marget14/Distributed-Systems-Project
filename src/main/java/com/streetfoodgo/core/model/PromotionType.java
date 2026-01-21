package com.streetfoodgo.core.model;

/**
 * Types of promotions available in Efood-like system.
 */
public enum PromotionType {
    /**
     * Fixed amount discount (e.g., -5€)
     */
    FIXED_DISCOUNT,

    /**
     * Percentage discount (e.g., 20% off)
     */
    PERCENTAGE_DISCOUNT,

    /**
     * Free delivery
     */
    FREE_DELIVERY,

    /**
     * Buy X get Y free (e.g., Buy 2 Get 1 Free)
     */
    BUY_X_GET_Y_FREE,

    /**
     * First order discount
     */
    FIRST_ORDER,

    /**
     * Bundle deal (specific items together)
     */
    BUNDLE_DEAL,

    /**
     * Minimum order amount discount
     */
    MINIMUM_ORDER_BONUS
}
