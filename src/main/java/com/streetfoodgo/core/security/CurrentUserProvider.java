package com.streetfoodgo.core.security;

import com.streetfoodgo.core.model.PersonType;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Component for providing the current user.
 *
 * @see CurrentUser
 */
@Component
public final class CurrentUserProvider {

    public Optional<CurrentUser> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return Optional.of(new CurrentUser(userDetails.personId(), userDetails.getUsername(), userDetails.type()));
        }

        // API JWT authentication uses Spring Security's User principal (see JwtAuthenticationFilter)
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User user) {
            final String subject = user.getUsername();
            if (subject != null && subject.startsWith("user:")) {
                final long personId = Long.parseLong(subject.substring("user:".length()));
                // Derive type from granted authorities
                final var authorities = user.getAuthorities();
                PersonType type = null;
                for (var ga : authorities) {
                    final String a = ga.getAuthority();
                    if ("ROLE_CUSTOMER".equals(a)) type = PersonType.CUSTOMER;
                    else if ("ROLE_OWNER".equals(a)) type = PersonType.OWNER;
                    else if ("ROLE_ADMIN".equals(a)) type = PersonType.ADMIN;
                }
                if (type != null) {
                    return Optional.of(new CurrentUser(personId, user.getUsername(), type));
                }
            }
        }

        return Optional.empty();
    }

    public CurrentUser requireCurrentUser() {
        return this.getCurrentUser().orElseThrow(() -> new SecurityException("not authenticated"));
    }

    public long requiredCustomerId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.type() != PersonType.CUSTOMER) throw new SecurityException("Customer type/role required");
        return currentUser.id();
    }
}
