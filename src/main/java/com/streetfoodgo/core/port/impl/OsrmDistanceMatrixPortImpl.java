package com.streetfoodgo.core.port.impl;

import com.streetfoodgo.core.port.DistanceMatrixPort;
import com.streetfoodgo.core.port.impl.dto.OsrmTableResponse;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for OSRM /table API (distance matrix).
 */
@Service
public class OsrmDistanceMatrixPortImpl implements DistanceMatrixPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsrmDistanceMatrixPortImpl.class);

    private final RestTemplate restTemplate;
    private final String osrmBaseUrl;

    public OsrmDistanceMatrixPortImpl(
            final RestTemplate restTemplate,
            @Value("${app.osrm.base-url:http://localhost:5000}") final String osrmBaseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (osrmBaseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.osrmBaseUrl = osrmBaseUrl;
    }

    @Override
    public List<MatrixResult> getMetrics(final double originLat, final double originLon, final List<LatLon> destinations) {
        if (destinations == null) throw new NullPointerException();
        if (destinations.isEmpty()) return List.of();

        // OSRM expects lon,lat.
        // Coordinates string is: origin;dest1;dest2;...
        final StringBuilder coords = new StringBuilder();
        coords.append(originLon).append(',').append(originLat);
        for (LatLon d : destinations) {
            coords.append(';').append(d.lon()).append(',').append(d.lat());
        }

        // sources=0 (origin), destinations=1;2;3...
        final StringBuilder destinationIdx = new StringBuilder();
        for (int i = 0; i < destinations.size(); i++) {
            if (i > 0) destinationIdx.append(';');
            destinationIdx.append(i + 1);
        }

        final String url = UriComponentsBuilder
                .fromUriString(osrmBaseUrl)
                .pathSegment("table", "v1", "driving")
                .path("/")
                .path(coords.toString())
                .queryParam("sources", "0")
                .queryParam("destinations", destinationIdx.toString())
                .queryParam("annotations", "duration,distance")
                .toUriString();

        try {
            ResponseEntity<OsrmTableResponse> response = restTemplate.getForEntity(url, OsrmTableResponse.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("OSRM responded with " + response.getStatusCode());
            }

            OsrmTableResponse body = response.getBody();
            if (body == null) {
                throw new RuntimeException("OSRM returned empty response");
            }

            // Expect 1xN for sources=0
            List<List<Double>> distances = body.distances();
            List<List<Double>> durations = body.durations();
            if (distances == null || durations == null || distances.isEmpty() || durations.isEmpty()) {
                throw new RuntimeException("OSRM returned empty matrix");
            }

            List<Double> distRow = distances.getFirst();
            List<Double> durRow = durations.getFirst();

            if (distRow.size() != destinations.size() || durRow.size() != destinations.size()) {
                throw new RuntimeException("OSRM matrix size mismatch");
            }

            List<MatrixResult> results = new ArrayList<>(destinations.size());
            for (int i = 0; i < destinations.size(); i++) {
                Double meters = distRow.get(i);
                Double seconds = durRow.get(i);

                // OSRM uses null for unreachable routes.
                if (meters == null || seconds == null) {
                    results.add(new MatrixResult(BigDecimal.ZERO, Integer.MAX_VALUE));
                    continue;
                }

                BigDecimal distanceKm = BigDecimal.valueOf(meters / 1000.0)
                        .setScale(2, RoundingMode.HALF_UP);
                int durationMinutes = (int) Math.max(0, Math.round(seconds / 60.0));

                results.add(new MatrixResult(distanceKm, durationMinutes));
            }

            return results;

        } catch (RestClientException ex) {
            LOGGER.warn("OSRM table call failed: {}", ex.getMessage());
            throw ex;
        }
    }
}
