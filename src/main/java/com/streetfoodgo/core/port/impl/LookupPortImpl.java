package com.streetfoodgo.core.port.impl;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.streetfoodgo.config.RestApiClientConfig;
import com.streetfoodgo.core.model.PersonType;
import com.streetfoodgo.core.port.LookupPort;
import com.streetfoodgo.core.port.impl.dto.LookupResult;

/**
 * Default implementation of {@link LookupPort}. It uses the NOC external service.
 */

@Service
public class LookupPortImpl implements LookupPort {
    private final RestTemplate restTemplate;
    private final String huaNocBaseUrl;

    public LookupPortImpl(RestTemplate restTemplate, String huaNocBaseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (huaNocBaseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.huaNocBaseUrl = huaNocBaseUrl;
    }

    @Override
    public Optional<PersonType> lookup(final String huaId) {
        if (huaId == null || huaId.isBlank()) throw new IllegalArgumentException();
        final String url = huaNocBaseUrl + "/api/v1/lookups/" + huaId;
        ResponseEntity<LookupResult> response = restTemplate.getForEntity(url, LookupResult.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            LookupResult lookupResult = response.getBody();
            if (lookupResult == null) throw new NullPointerException();
            return Optional.ofNullable(lookupResult.type());
        }
        throw new RuntimeException("External service responded with " + response.getStatusCode());
    }
}

