package com.streetfoodgo.core.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides routing information (distance, duration, and route geometry) via an external service (OSRM)
 * with a safe fallback when the external service is unavailable.
 */
public interface RoutingProxyService {

    RouteWithGeometry getRoute(double fromLat, double fromLon, double toLat, double toLon);

    record RouteWithGeometry(
            BigDecimal distanceKm,
            Integer durationMinutes,
            GeoJsonLineString geometry,
            boolean fallback
    ) {}

    /**
     * Minimal GeoJSON LineString.
     * Coordinates are in [lon, lat] order, as per GeoJSON spec.
     */
    record GeoJsonLineString(String type, List<List<Double>> coordinates) {
        public GeoJsonLineString {
            if (type == null) throw new NullPointerException("type");
            if (coordinates == null) throw new NullPointerException("coordinates");
        }

        public static GeoJsonLineString lineString(List<List<Double>> coordinates) {
            return new GeoJsonLineString("LineString", coordinates);
        }
    }
}
