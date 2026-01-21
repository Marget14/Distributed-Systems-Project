package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.DeliveryAddressView;
import com.streetfoodgo.core.service.model.StoreView;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for geolocation and distance calculations.
 */
public interface GeolocationService {

    /**
     * Calculate distance between two coordinates in kilometers.
     *
     * @param storeLatitude Store latitude
     * @param storeLongitude Store longitude
     * @param customerLatitude Customer latitude
     * @param customerLongitude Customer longitude
     * @return Distance in kilometers
     */
    BigDecimal calculateDistance(BigDecimal storeLatitude, BigDecimal storeLongitude,
                                 BigDecimal customerLatitude, BigDecimal customerLongitude);

    /**
     * Calculate estimated delivery time in minutes.
     *
     * @param distanceKm Distance in kilometers
     * @return Estimated delivery time in minutes
     */
    Integer calculateEstimatedDeliveryTime(BigDecimal distanceKm);

    /**
     * Calculate distance and ETA for delivery.
     *
     * @param store Store view with coordinates
     * @param deliveryAddress Delivery address with coordinates
     * @return Array with [distance_km, estimated_minutes]
     */
    DeliveryMetrics calculateDeliveryMetrics(StoreView store, DeliveryAddressView deliveryAddress);

    /**
     * Compute and sort stores by estimated delivery time for the given delivery address.
     *
     * Used for "nearby stores" UX (efood-like ordering): show the closest/fastest stores first.
     *
     * @param stores candidate stores
     * @param deliveryAddress customer delivery address
     * @param limit max stores to return (<=0 means no limit)
     */
    List<StoreWithMetrics> rankStoresByDeliveryTime(List<StoreView> stores, DeliveryAddressView deliveryAddress, int limit);

    /**
     * Record for delivery metrics.
     */
    record DeliveryMetrics(BigDecimal distanceKm, Integer estimatedMinutes) {}

    /**
     * Record for store with metrics.
     */
    record StoreWithMetrics(StoreView store, DeliveryMetrics metrics) {}
}
