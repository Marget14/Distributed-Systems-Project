package com.streetfoodgo.core.port.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.streetfoodgo.config.RestApiClientConfig;
import com.streetfoodgo.core.port.SmsNotificationPort;
import com.streetfoodgo.core.port.impl.dto.SendSmsRequest;
import com.streetfoodgo.core.port.impl.dto.SendSmsResult;

/**
 * Default implementation of {@link SmsNotificationPort}. It uses the NOC external service.
 */

@Service
public class SmsNotificationPortImpl implements SmsNotificationPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsNotificationPortImpl.class);
    private static final boolean ACTIVE = false;
    private final RestTemplate restTemplate;
    private final String huaNocBaseUrl;

    public SmsNotificationPortImpl(RestTemplate restTemplate, String huaNocBaseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (huaNocBaseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.huaNocBaseUrl = huaNocBaseUrl;
    }

    @Override
    public boolean sendSms(final String e164, final String content) {
        if (e164 == null || e164.isBlank() || content == null || content.isBlank()) throw new IllegalArgumentException();
        if (!ACTIVE) {
            LOGGER.warn("SMS Notification is not active");
            return true;
        }
        if (e164.startsWith("+30692") || e164.startsWith("+30690000")) {
            LOGGER.warn("Not allocated E164 {}. Aborting...", e164);
            return true;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        SendSmsRequest body = new SendSmsRequest(e164, content);
        HttpEntity<SendSmsRequest> entity = new HttpEntity<>(body, headers);
        String url = huaNocBaseUrl + "/api/v1/sms";
        ResponseEntity<SendSmsResult> response = restTemplate.postForEntity(url, entity, SendSmsResult.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            SendSmsResult sendSmsResult = response.getBody();
            if (sendSmsResult == null) throw new NullPointerException();
            return sendSmsResult.sent();
        }
        throw new RuntimeException("External service responded with " + response.getStatusCode());
    }
}

