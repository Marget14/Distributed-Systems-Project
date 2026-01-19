package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.port.impl.dto.OsrmRouteGeoResponse;
import com.streetfoodgo.core.service.RoutingProxyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RoutingProxyServiceImpl implements RoutingProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingProxyServiceImpl.class);

    // Average city delivery speed (km/h) for ETA fallback
    private static final int AVERAGE_SPEED_KMH = 30;

    private final RestTemplate restTemplate;
    private final String osrmBaseUrl;

    public RoutingProxyServiceImpl(
            final RestTemplate restTemplate,
            @Value("${app.osrm.base-url:http://localhost:5000}") final String osrmBaseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (osrmBaseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.osrmBaseUrl = osrmBaseUrl;
    }

    @Override
    public RouteWithGeometry getRoute(double fromLat, double fromLon, double toLat, double toLon) {
        final String url = UriComponentsBuilder
                .fromUriString(osrmBaseUrl)
                .pathSegment("route", "v1", "driving")
                .path("/")
                .path(fromLon + "," + fromLat + ";" + toLon + "," + toLat)
                .queryParam("overview", "full")
                .queryParam("alternatives", "false")
                .queryParam("steps", "false")
                .queryParam("geometries", "geojson")
                .toUriString();

        try {
            ResponseEntity<OsrmRouteGeoResponse> response = restTemplate.getForEntity(url, OsrmRouteGeoResponse.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("OSRM responded with " + response.getStatusCode());
            }

            OsrmRouteGeoResponse body = response.getBody();
            if (body == null || body.routes() == null || body.routes().isEmpty()) {
                throw new RuntimeException("OSRM returned empty routes");
            }

            OsrmRouteGeoResponse.Route r = body.routes().getFirst();
            BigDecimal distanceKm = BigDecimal.valueOf(r.distance() / 1000.0)
                    .setScale(2, RoundingMode.HALF_UP);
            int durationMinutes = (int) Math.max(0, Math.round(r.duration() / 60.0));

            GeoJsonLineString geometry;
            if (r.geometry() != null && r.geometry().coordinates() != null && !r.geometry().coordinates().isEmpty()) {
                geometry = new GeoJsonLineString(
                        r.geometry().type() != null ? r.geometry().type() : "LineString",
                        r.geometry().coordinates()
                );
            } else {
                geometry = fallbackGeometry(fromLat, fromLon, toLat, toLon);
            }

            return new RouteWithGeometry(distanceKm, durationMinutes, geometry, false);

        } catch (RestClientException ex) {
            LOGGER.warn("Routing proxy fallback due to OSRM client error: {}", ex.getMessage());
            return fallbackRoute(fromLat, fromLon, toLat, toLon);
        } catch (RuntimeException ex) {
            LOGGER.warn("Routing proxy fallback due to OSRM response error: {}", ex.getMessage());
            return fallbackRoute(fromLat, fromLon, toLat, toLon);
        }
    }

    private RouteWithGeometry fallbackRoute(double fromLat, double fromLon, double toLat, double toLon) {
        BigDecimal distanceKm = haversineKm(fromLat, fromLon, toLat, toLon)
                .setScale(2, RoundingMode.HALF_UP);

        int durationMinutes;
        if (distanceKm.signum() <= 0) durationMinutes = 0;
        else {
            double hours = distanceKm.doubleValue() / AVERAGE_SPEED_KMH;
            durationMinutes = (int) Math.max(1, Math.round(hours * 60.0));
        }

        return new RouteWithGeometry(distanceKm, durationMinutes, fallbackGeometry(fromLat, fromLon, toLat, toLon), true);
    }

    private GeoJsonLineString fallbackGeometry(double fromLat, double fromLon, double toLat, double toLon) {
        return GeoJsonLineString.lineString(List.of(
                List.of(fromLon, fromLat),
                List.of(toLon, toLat)
        ));
    }

    private BigDecimal haversineKm(double lat1, double lon1, double lat2, double lon2) {
        // Earth radius in km
        final double earthRadiusKm = 6371.0;
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadiusKm * c);
    }
}
