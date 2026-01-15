package com.streetfoodgo.web.rest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

/**
 * Helper utilities for REST security context.
 */
public final class RestSecurityUtils {

    private RestSecurityUtils() {}

    public static long requireUserId(final Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("not authenticated");
        }

        if (authentication.getPrincipal() instanceof User user) {
            final String username = user.getUsername();
            // JWT subject set by JwtService in UserAuthResource is: user:{personId}
            if (username != null && username.startsWith("user:")) {
                final String idPart = username.substring("user:".length());
                return Long.parseLong(idPart);
            }
        }

        throw new SecurityException("authenticated user token required");
    }
}
