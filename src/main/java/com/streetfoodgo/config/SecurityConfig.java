package com.streetfoodgo.config;

import com.streetfoodgo.core.security.JwtAuthenticationFilter;
import com.streetfoodgo.web.rest.error.RestApiAccessDeniedHandler;
import com.streetfoodgo.web.rest.error.RestApiAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
                        .requestMatchers("/api/v1/stores/**").permitAll() // Public store viewing
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
    public SecurityFilterChain uiChain(final HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/", "/stores", "/stores/**", "/login", "/register").permitAll()
                        .requestMatchers("/api/cart/**").permitAll()  // ✅ ΠΡΟΣΘΗΚΗ - Allow cart API for guests
                        .requestMatchers("/profile", "/profile/**", "/logout", "/cart", "/orders").authenticated()
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/profile", true)
                        .failureUrl("/login?error")
                        .permitAll()
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
