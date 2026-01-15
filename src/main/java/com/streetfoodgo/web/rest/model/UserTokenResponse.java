package com.streetfoodgo.web.rest.model;

import java.util.List;

public record UserTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        long userId,
        String email,
        String userType,
        List<String> roles
) {}
