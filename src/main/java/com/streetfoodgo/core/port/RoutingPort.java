package com.streetfoodgo.core.port;

import java.math.BigDecimal;

/**
 * Port to an external routing service (e.g. OSRM) for distance + duration estimation.
 */
public interface RoutingPort {

    RouteMetrics getRoute(double fromLat, double fromLon, double toLat, double toLon);

    record RouteMetrics(BigDecimal distanceKm, Integer durationMinutes) {}
}
