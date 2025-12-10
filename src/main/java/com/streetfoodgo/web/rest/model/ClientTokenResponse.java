package com.streetfoodgo.web.rest.model;

import com.streetfoodgo.web.rest.ClientAuthResource;

/**
 * @see ClientAuthResource
 */
public record ClientTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
