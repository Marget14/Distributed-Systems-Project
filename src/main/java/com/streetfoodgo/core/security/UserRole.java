package com.streetfoodgo.core.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Set;
import java.util.stream.Collectors;

import static com.streetfoodgo.core.security.UserPermission.*;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER(Set.of(ORDER_CREATE, ORDER_READ)),
    OWNER(Set.of(ORDER_READ, ORDER_UPDATE_STATUS, MENU_MANAGE)),
    ADMIN(Set.of(ORDER_READ, ORDER_UPDATE_STATUS, MENU_MANAGE, USER_MANAGE));

    private final Set<UserPermission> permissions;

    /**
     * Αυτή η μέθοδος μετατρέπει τα Permissions σε GrantedAuthorities
     * που μπορεί να διαβάσει το Spring Security.
     */
    public Set<SimpleGrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                .collect(Collectors.toSet());

        // Προσθέτουμε και το ίδιο το Role με το πρόθεμα ROLE_ (π.χ. ROLE_ADMIN)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}