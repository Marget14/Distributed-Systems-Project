package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.service.GeolocationService;
import com.streetfoodgo.core.service.model.DeliveryAddressView;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.core.port.RoutingPort;
import com.streetfoodgo.core.port.DistanceMatrixPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of GeolocationService using Haversine formula.
 * Calculates distance between two geographic points.
 */
@Service
public class GeolocationServiceImpl implements GeolocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationServiceImpl.class);

    // Average delivery speed in km/h (for food delivery motorcycles)
    private static final int AVERAGE_DELIVERY_SPEED_KMH = 30;

    private final RoutingPort routingPort;
    private final DistanceMatrixPort distanceMatrixPort;

    public GeolocationServiceImpl(final RoutingPort routingPort, final DistanceMatrixPort distanceMatrixPort) {
        if (routingPort == null) throw new NullPointerException();
        if (distanceMatrixPort == null) throw new NullPointerException();
        this.routingPort = routingPort;
        this.distanceMatrixPort = distanceMatrixPort;
    }

    @Override
    public BigDecimal calculateDistance(BigDecimal storeLatitude, BigDecimal storeLongitude,
                                        BigDecimal customerLatitude, BigDecimal customerLongitude) {

        if (storeLatitude == null || storeLongitude == null ||
            customerLatitude == null || customerLongitude == null) {
            LOGGER.warn("Missing coordinates for distance calculation");
            return BigDecimal.ZERO;
        }

        // Haversine formula
        double lat1Rad = Math.toRadians(storeLatitude.doubleValue());
        double lon1Rad = Math.toRadians(storeLongitude.doubleValue());
        double lat2Rad = Math.toRadians(customerLatitude.doubleValue());
        double lon2Rad = Math.toRadians(customerLongitude.doubleValue());

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Earth's radius in kilometers
        double earthRadiusKm = 6371.0;
        double distanceKm = earthRadiusKm * c;

        LOGGER.debug("Calculated distance: {} km", distanceKm);

        return BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Integer calculateEstimatedDeliveryTime(BigDecimal distanceKm) {
        if (distanceKm == null || distanceKm.signum() <= 0) {
            return 0;
        }

        // Time = Distance / Speed
        // Add buffer time for preparation, pickup, etc.
        double timeInHours = distanceKm.doubleValue() / AVERAGE_DELIVERY_SPEED_KMH;
        int timeInMinutes = (int) (timeInHours * 60);

        // Add 10 minutes for preparation and pickup
        int totalMinutes = Math.max(15, timeInMinutes + 10);

        LOGGER.debug("Estimated delivery time: {} minutes for {} km", totalMinutes, distanceKm);

        return totalMinutes;
    }

    @Override
    public DeliveryMetrics calculateDeliveryMetrics(StoreView store, DeliveryAddressView deliveryAddress) {
        if (store == null || deliveryAddress == null) {
            return new DeliveryMetrics(BigDecimal.ZERO, 0);
        }

        // Prefer external routing service if we have coordinates.
        if (store.latitude() != null && store.longitude() != null &&
            deliveryAddress.latitude() != null && deliveryAddress.longitude() != null) {

            try {
                RoutingPort.RouteMetrics route = routingPort.getRoute(
                        store.latitude(), store.longitude(),
                        deliveryAddress.latitude(), deliveryAddress.longitude()
                );

                // Add a realistic buffer for prep/pickup similar to how apps behave.
                int withPrepBuffer = Math.max(15, route.durationMinutes() + 20);
                return new DeliveryMetrics(route.distanceKm(), withPrepBuffer);

            } catch (RestClientException ex) {
                // Fallback to local approximation.
                LOGGER.warn("Falling back to Haversine delivery metrics due to routing error");
            }
        }

        BigDecimal distance = calculateDistance(
                store.latitude() != null ? BigDecimal.valueOf(store.latitude()) : BigDecimal.ZERO,
                store.longitude() != null ? BigDecimal.valueOf(store.longitude()) : BigDecimal.ZERO,
                deliveryAddress.latitude() != null ? BigDecimal.valueOf(deliveryAddress.latitude()) : BigDecimal.ZERO,
                deliveryAddress.longitude() != null ? BigDecimal.valueOf(deliveryAddress.longitude()) : BigDecimal.ZERO
        );

        Integer estimatedMinutes = calculateEstimatedDeliveryTime(distance);

        return new DeliveryMetrics(distance, estimatedMinutes);
    }

    @Override
    public List<StoreWithMetrics> rankStoresByDeliveryTime(final List<StoreView> stores,
                                                          final DeliveryAddressView deliveryAddress,
                                                          final int limit) {
        if (stores == null) throw new NullPointerException();
        if (deliveryAddress == null) throw new NullPointerException();
        if (stores.isEmpty()) return List.of();

        // If we don't have customer coordinates, we can't compute meaningful ranking.
        if (deliveryAddress.latitude() == null || deliveryAddress.longitude() == null) {
            return stores.stream()
                    .map(s -> new StoreWithMetrics(s, new DeliveryMetrics(BigDecimal.ZERO, s.estimatedDeliveryTimeMinutes() != null ? s.estimatedDeliveryTimeMinutes() : 0)))
                    .limit(limit > 0 ? limit : Long.MAX_VALUE)
                    .toList();
        }

        // Prefer OSRM /table for performance (one call).
        try {
            final List<StoreView> eligible = stores.stream()
                    .filter(s -> s.latitude() != null && s.longitude() != null)
                    .toList();

            if (!eligible.isEmpty()) {
                List<DistanceMatrixPort.LatLon> destinations = eligible.stream()
                        .map(s -> new DistanceMatrixPort.LatLon(s.latitude(), s.longitude()))
                        .toList();

                List<DistanceMatrixPort.MatrixResult> matrix = distanceMatrixPort.getMetrics(
                        deliveryAddress.latitude(), deliveryAddress.longitude(),
                        destinations
                );

                List<StoreWithMetrics> ranked = new ArrayList<>(eligible.size());
                for (int i = 0; i < eligible.size(); i++) {
                    DistanceMatrixPort.MatrixResult r = matrix.get(i);
                    int withPrepBuffer = Math.max(15, r.durationMinutes() + 20);
                    ranked.add(new StoreWithMetrics(eligible.get(i), new DeliveryMetrics(r.distanceKm(), withPrepBuffer)));
                }

                ranked.sort(Comparator.comparingInt(swm -> swm.metrics().estimatedMinutes()));

                // Add the remaining stores (without coordinates) at the end.
                stores.stream()
                        .filter(s -> s.latitude() == null || s.longitude() == null)
                        .forEach(s -> ranked.add(new StoreWithMetrics(s, new DeliveryMetrics(BigDecimal.ZERO, Integer.MAX_VALUE))));

                return ranked.stream()
                        .limit(limit > 0 ? limit : Long.MAX_VALUE)
                        .toList();
            }

        } catch (RestClientException ex) {
            LOGGER.warn("Falling back to per-store routing due to OSRM table error");
        }

        // Fallback: compute individually (uses route/haversine fallback inside).
        return stores.stream()
                .map(s -> new StoreWithMetrics(s, calculateDeliveryMetrics(s, deliveryAddress)))
                .sorted(Comparator.comparingInt(swm -> swm.metrics().estimatedMinutes()))
                .limit(limit > 0 ? limit : Long.MAX_VALUE)
                .toList();
    }
}
