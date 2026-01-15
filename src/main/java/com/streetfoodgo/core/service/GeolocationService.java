package com.streetfoodgo.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Service for geolocation and delivery time estimation.
 *
 * Current implementation:
 * - OSRM (self-hosted, via docker-compose) for route distance/duration
 * - Nominatim (OpenStreetMap) for geocoding fallback
 * - Haversine distance fallback if OSRM is unavailable

 */
@Service
public class GeolocationService {

    private final RestTemplate restTemplate;

    @Value("${app.osrm.base-url:http://localhost:5000}")
    private String osrmBaseUrl;

    private static final int PREPARATION_TIME = 15; // Base preparation time in minutes

    public GeolocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calculate estimated delivery time based on real distance.
     */
    public int calculateDeliveryTime(String storeAddress, String deliveryAddress) {
        try {
            // Get coordinates for both addresses
            Coordinates storeCoords = geocodeAddress(storeAddress);
            Coordinates deliveryCoords = geocodeAddress(deliveryAddress);

            if (storeCoords == null || deliveryCoords == null) {
                return PREPARATION_TIME + 20; // Fallback
            }

            // Calculate real driving distance
            double distanceKm = calculateDrivingDistance(storeCoords, deliveryCoords);

            final int travelTimeMinutes = calculateDrivingDurationMinutes(storeCoords, deliveryCoords);
            return PREPARATION_TIME + travelTimeMinutes;

        } catch (Exception e) {
            System.err.println("Geolocation error: " + e.getMessage());
            return PREPARATION_TIME + 20; // Fallback
        }
    }

    /**
     * Calculate driving distance in KM using OSRM.
     */
    public double calculateDrivingDistance(Coordinates start, Coordinates end) {
        try {
            final String url = osrmBaseUrl + "/route/v1/driving/"
                    + start.lng + "," + start.lat + ";" + end.lng + "," + end.lat
                    + "?overview=false";

            final var response = restTemplate.getForObject(url, com.streetfoodgo.core.service.osrm.OsrmRouteResponse.class);
            if (response != null && response.routes() != null && !response.routes().isEmpty()) {
                final double meters = response.routes().get(0).distance();
                return meters / 1000.0;
            }
        } catch (Exception e) {
            System.err.println("OSRM driving distance error: " + e.getMessage());
        }

        // Fallback to straight-line distance
        return calculateHaversineDistance(start, end);
    }

    /**
     * Calculate driving duration in minutes using OSRM.
     */
    public int calculateDrivingDurationMinutes(Coordinates start, Coordinates end) {
        try {
            final String url = osrmBaseUrl + "/route/v1/driving/"
                    + start.lng + "," + start.lat + ";" + end.lng + "," + end.lat
                    + "?overview=false";

            final var response = restTemplate.getForObject(url, com.streetfoodgo.core.service.osrm.OsrmRouteResponse.class);
            if (response != null && response.routes() != null && !response.routes().isEmpty()) {
                final double seconds = response.routes().get(0).duration();
                return (int) Math.ceil(seconds / 60.0);
            }
        } catch (Exception e) {
            System.err.println("OSRM driving duration error: " + e.getMessage());
        }
        // fallback: estimate based on haversine distance and average speed
        final double km = calculateHaversineDistance(start, end);
        final double avgSpeedKmh = 30.0;
        return (int) Math.ceil((km / avgSpeedKmh) * 60.0);
    }

    /**
     * Geocode address to coordinates using Nominatim (free, no API key needed).
     */
    public Coordinates geocodeAddress(String address) {
        try {
            // Clean up address
            String cleanAddress = address.trim();
            if (!cleanAddress.toLowerCase().contains("greece") && !cleanAddress.toLowerCase().contains("athens")) {
                cleanAddress += ", Athens, Greece";
            }

            String url = "https://nominatim.openstreetmap.org/search"
                    + "?q=" + cleanAddress.replace(" ", "+")
                    + "&format=json"
                    + "&limit=1";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "StreetFoodGo/1.0");

            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> results = response.getBody();
            if (results != null && !results.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) results.get(0);
                double lat = Double.parseDouble(result.get("lat").toString());
                double lon = Double.parseDouble(result.get("lon").toString());
                return new Coordinates(lat, lon);
            }
        } catch (Exception e) {
            System.err.println("Geocoding error for address '" + address + "': " + e.getMessage());
        }

        // Fallback: Athens center coordinates
        return new Coordinates(37.9838, 23.7275);
    }

    /**
     * Calculate straight-line distance using Haversine formula.
     */
    public double calculateHaversineDistance(Coordinates start, Coordinates end) {
        final int R = 6371; // Earth's radius in km

        double latDistance = Math.toRadians(end.lat - start.lat);
        double lonDistance = Math.toRadians(end.lng - start.lng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(start.lat)) * Math.cos(Math.toRadians(end.lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Coordinates class
     */
    public static class Coordinates {
        public final double lat;
        public final double lng;

        public Coordinates(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        public String toString() {
            return "Coordinates{lat=" + lat + ", lng=" + lng + '}';
        }
    }
}
