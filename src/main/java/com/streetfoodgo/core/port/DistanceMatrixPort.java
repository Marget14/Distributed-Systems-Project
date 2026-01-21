package com.streetfoodgo.core.port;

import java.math.BigDecimal;
import java.util.List;

/**
 * Port to an external geolocation service that can provide a distance matrix (many destinations).
 *
 * Typical implementation: OSRM /table API.
 */
public interface DistanceMatrixPort {

    /**
     * Calculate distance+duration from one origin to many destinations.
     *
     * @param originLat origin latitude
     * @param originLon origin longitude
     * @param destinations list of [lat,lon] points
     */
    List<MatrixResult> getMetrics(double originLat, double originLon, List<LatLon> destinations);

    record LatLon(double lat, double lon) {}

    /**
     * One-to-one result aligned by index with the destinations list.
     */
    record MatrixResult(BigDecimal distanceKm, Integer durationMinutes) {}
}
