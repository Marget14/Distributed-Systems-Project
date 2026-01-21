package com.streetfoodgo.config;

import com.streetfoodgo.core.security.JwtAuthenticationFilter;
import com.streetfoodgo.core.security.LoginNotificationSuccessHandler;
import com.streetfoodgo.web.rest.error.RestApiAccessDeniedHandler;
import com.streetfoodgo.web.rest.error.RestApiAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for StreetFoodGo.
 *
 * Two security chains:
 * 1. API chain (stateless, JWT-based) for /api/v1/**
 * 2. UI chain (stateful, session-based) for web interface
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * API chain for REST API (/api/v1/**) - JWT authentication
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(
            final HttpSecurity http,
            final JwtAuthenticationFilter jwtAuthenticationFilter,
            final RestApiAuthenticationEntryPoint restApiAuthenticationEntryPoint,
            final RestApiAccessDeniedHandler restApiAccessDeniedHandler) throws Exception {

        http
                .securityMatcher("/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/client-tokens").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/stores/**").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                )
                .exceptionHandling(exh -> exh
                        .authenticationEntryPoint(restApiAuthenticationEntryPoint)
                        .accessDeniedHandler(restApiAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * UI chain for web interface (/**) - session-based authentication
     */
    @Bean
    @Order(2)
    public SecurityFilterChain uiChain(final HttpSecurity http, final LoginNotificationSuccessHandler successHandler) throws Exception {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/", "/stores", "/stores/**", "/login", "/register").permitAll()
                        .requestMatchers("/auth/verify-email", "/auth/resend-verification").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/**").permitAll()

                        .requestMatchers("/api/cart/checkout").authenticated()

                        .requestMatchers("/profile", "/profile/**", "/logout").authenticated()
                        .requestMatchers("/cart", "/checkout").hasRole("CUSTOMER")
                        .requestMatchers("/orders", "/orders/**").hasRole("CUSTOMER")
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com https://cdn.tailwindcss.com https://cdnjs.cloudflare.com; " +
                                        "style-src 'self' 'unsafe-inline' blob: https://unpkg.com https://fonts.googleapis.com https://cdnjs.cloudflare.com https://cdn.tailwindcss.com; " +
                                        "img-src 'self' data: blob: https://*.tile.openstreetmap.org https://tile.openstreetmap.org https://unpkg.com https://*.basemaps.cartocdn.com https://cdn-icons-png.flaticon.com https://images.unsplash.com; " +
                                        "font-src 'self' https://fonts.gstatic.com data: https://cdnjs.cloudflare.com; " +
                                        "connect-src 'self' ws://localhost:8080 wss://localhost:8080 http://localhost:8080 http://localhost:5000 http://osrm:5000 https://unpkg.com https://nominatim.openstreetmap.org http://router.project-osrm.org;")
                        )
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler) // Use custom success handler
                        .failureUrl("/login?error")
                        .permitAll()
                )
                // Remember-me cookie for every successful login
                .rememberMe(rm -> rm
                        .alwaysRemember(true)
                        .key("streetfoodgo-remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
