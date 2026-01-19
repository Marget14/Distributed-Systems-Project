package com.streetfoodgo.core.port.impl;

import com.streetfoodgo.core.port.RoutingPort;
import com.streetfoodgo.core.port.impl.dto.OsrmRouteResponse;
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

/**
 * Adapter for OSRM /route API.
 */
@Service
public class OsrmRoutingPortImpl implements RoutingPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsrmRoutingPortImpl.class);

    private final RestTemplate restTemplate;
    private final String osrmBaseUrl;

    public OsrmRoutingPortImpl(
            final RestTemplate restTemplate,
            @Value("${app.osrm.base-url:http://localhost:5000}") final String osrmBaseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (osrmBaseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.osrmBaseUrl = osrmBaseUrl;
    }

    @Override
    public RouteMetrics getRoute(double fromLat, double fromLon, double toLat, double toLon) {
        // OSRM expects lon,lat order.
        final String url = UriComponentsBuilder
                .fromUriString(osrmBaseUrl)
                .pathSegment("route", "v1", "driving")
                .path("/")
                .path(fromLon + "," + fromLat + ";" + toLon + "," + toLat)
                .queryParam("overview", "false")
                .queryParam("alternatives", "false")
                .queryParam("steps", "false")
                .toUriString();

        try {
            ResponseEntity<OsrmRouteResponse> response = restTemplate.getForEntity(url, OsrmRouteResponse.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("OSRM responded with " + response.getStatusCode());
            }
            OsrmRouteResponse body = response.getBody();
            if (body == null || body.routes() == null || body.routes().isEmpty()) {
                throw new RuntimeException("OSRM returned empty routes");
            }

            OsrmRouteResponse.Route r = body.routes().getFirst();

            BigDecimal distanceKm = BigDecimal.valueOf(r.distance() / 1000.0)
                    .setScale(2, RoundingMode.HALF_UP);
            int durationMinutes = (int) Math.max(0, Math.round(r.duration() / 60.0));

            return new RouteMetrics(distanceKm, durationMinutes);

        } catch (RestClientException ex) {
            LOGGER.warn("OSRM call failed: {}", ex.getMessage());
            throw ex;
        }
    }
}
