package com.streetfoodgo.core.service.model;

import java.time.Instant;

/**
 * View/DTO for DeliveryAddress entity.
 */
public record DeliveryAddressView(
        Long id,
        Long customerId,
        String label,
        String street,
        String city,
        Long number,
        String area,
        String postalCode,
        String phoneNumber,
        Double latitude,
        Double longitude,
        Boolean isDefault,
        Instant createdAt
) {
    public String fullAddress() {
        return street + (number != null ? " " + number : "") + ", " + city + " " + postalCode;
    }
}