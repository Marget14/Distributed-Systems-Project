package com.streetfoodgo.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Service for geolocation and delivery time estimation using OpenRouteService API.
 *
 * Get free API key from: https://openrouteservice.org/dev/#/signup
 */
@Service
public class GeolocationService {

    private final RestTemplate restTemplate;

    @Value("${app.openrouteservice.api-key:}")
    private String apiKey;

    private static final String ORS_BASE_URL = "https://api.openrouteservice.org";
    private static final double AVG_SPEED_KMH = 30.0; // Average delivery speed
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

            // Calculate travel time
            int travelTime = (int) Math.ceil((distanceKm / AVG_SPEED_KMH) * 60);

            return PREPARATION_TIME + travelTime;

        } catch (Exception e) {
            System.err.println("Geolocation error: " + e.getMessage());
            return PREPARATION_TIME + 20; // Fallback
        }
    }

    /**
     * Calculate driving distance between two coordinates.
     */
    public double calculateDrivingDistance(Coordinates start, Coordinates end) {
        if (apiKey == null || apiKey.isEmpty()) {
            // Fallback to straight-line distance
            return calculateHaversineDistance(start, end);
        }

        try {
            String url = ORS_BASE_URL + "/v2/directions/driving-car"
                    + "?start=" + start.lng + "," + start.lat
                    + "&end=" + end.lng + "," + end.lat;

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", apiKey);

            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("features")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> features = (List<Map<String, Object>>) body.get("features");
                if (!features.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> properties = (Map<String, Object>) features.get(0).get("properties");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> summary = (Map<String, Object>) properties.get("summary");
                    Number distanceMeters = (Number) summary.get("distance");
                    return distanceMeters.doubleValue() / 1000.0; // Convert to km
                }
            }
        } catch (Exception e) {
            System.err.println("Driving distance API error: " + e.getMessage());
        }

        // Fallback to straight-line distance
        return calculateHaversineDistance(start, end);
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
    private double calculateHaversineDistance(Coordinates start, Coordinates end) {
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
