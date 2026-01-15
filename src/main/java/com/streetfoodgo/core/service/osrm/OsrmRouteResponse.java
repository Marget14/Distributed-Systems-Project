package com.streetfoodgo.core.service.osrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmRouteResponse(
        List<Route> routes,
        List<Waypoint> waypoints,
        String code
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(
            double distance, // meters
            double duration  // seconds
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Waypoint(
            String name,
            List<Double> location
    ) {}
}
