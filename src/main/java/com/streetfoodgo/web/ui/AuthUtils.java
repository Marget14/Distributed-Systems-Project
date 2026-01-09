package com.streetfoodgo.web.ui;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Utility class for authentication checks in controllers.
 */
final class AuthUtils {

    private AuthUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isAuthenticated(final Authentication auth) {
        if (auth == null) return false;
        if (auth instanceof AnonymousAuthenticationToken) return false;
        return auth.isAuthenticated();
    }

    public static boolean isAnonymous(final Authentication auth) {
        return !isAuthenticated(auth);
    }
}