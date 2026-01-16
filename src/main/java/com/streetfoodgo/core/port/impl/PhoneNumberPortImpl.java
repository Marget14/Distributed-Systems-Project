package com.streetfoodgo.core.port.impl;

import com.streetfoodgo.core.port.PhoneNumberPort;
import com.streetfoodgo.core.port.impl.dto.PhoneNumberValidationResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Default implementation of {@link PhoneNumberPort}. It uses the NOC external service.
 */
@Service
public class PhoneNumberPortImpl implements PhoneNumberPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PhoneNumberPortImpl(final RestTemplate restTemplate,
                               @Value("${app.hua-noc.base-url:http://localhost:8081}") final String baseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (baseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public PhoneNumberValidationResult validate(final String rawPhoneNumber) {
        if (rawPhoneNumber == null) throw new NullPointerException();
        if (rawPhoneNumber.isBlank()) throw new IllegalArgumentException();

        final String url = baseUrl + "/api/v1/phone-numbers/" + rawPhoneNumber + "/validations";
        final ResponseEntity<PhoneNumberValidationResult> response
                = this.restTemplate.getForEntity(url, PhoneNumberValidationResult.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            final PhoneNumberValidationResult phoneNumberValidationResult = response.getBody();
            if (phoneNumberValidationResult == null) throw new NullPointerException();
            return phoneNumberValidationResult;
        }

        throw new RuntimeException("External service responded with " + response.getStatusCode());
    }
}