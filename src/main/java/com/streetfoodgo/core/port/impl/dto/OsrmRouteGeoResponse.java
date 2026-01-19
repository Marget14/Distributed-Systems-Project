package com.streetfoodgo.core.port.impl.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO for OSRM /route response when geometries=geojson.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmRouteGeoResponse(String code, List<Route> routes) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(double distance, double duration, Geometry geometry) {
        // distance: meters, duration: seconds
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(String type, List<List<Double>> coordinates) {
        // GeoJSON: coordinates in [lon, lat]
    }
}
