package com.streetfoodgo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for StreetFoodGo API.
 *
 * <p>Configures JWT Bearer token authentication scheme and creates API documentation
 * accessible at: <a href="http://localhost:8080/swagger-ui.html">Swagger UI</a></p>
 *
 * <p>The OpenAPI specification (JSON) is available at:
 * <a href="http://localhost:8080/v3/api-docs">API Docs JSON</a></p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Main OpenAPI specification bean.
     * Configures API title, version, contact info, and security schemes.
     *
     * @return configured OpenAPI specification
     */
    @Bean
    public OpenAPI openAPI() {
        final String description = """
                REST API for StreetFoodGo - A distributed food delivery platform.
                
                ## Authentication
                Most endpoints require JWT Bearer token authentication. \
                Obtain a token via POST /api/v1/auth/login with email and password.
                
                ## Roles
                - **CUSTOMER**: Can browse stores, place orders, track deliveries
                - **OWNER**: Can manage stores, menus, and process customer orders
                - **ADMIN**: Full system access
                - **INTEGRATION_READ**: Can read all data (integration clients)
                
                ## Base URL
                All endpoints are prefixed with: `/api/v1`
                """;

        return new OpenAPI()
                .info(new Info()
                        .title("StreetFoodGo API")
                        .version("v1.0.0")
                        .description(description)
                        .contact(new Contact()
                                .name("StreetFoodGo Development Team")
                                .email("support@streetfoodgo.com")
                                .url("https://github.com/streetfoodgo"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token. Example: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * Public API group - all public REST endpoints.
     *
     * @return grouped OpenAPI specification for public endpoints
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public REST API")
                .packagesToScan("com.streetfoodgo.web.rest")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}