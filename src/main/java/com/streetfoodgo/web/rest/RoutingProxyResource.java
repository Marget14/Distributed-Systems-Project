package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.service.RoutingProxyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping(value = "/api/v1/routing", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Routing", description = "Routing proxy endpoints (OSRM) for distance and live tracking")
public class RoutingProxyResource {

    private final RoutingProxyService routingProxyService;

    public RoutingProxyResource(final RoutingProxyService routingProxyService) {
        if (routingProxyService == null) throw new NullPointerException();
        this.routingProxyService = routingProxyService;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER')")
    @GetMapping("/route")
    @Operation(
            summary = "Get route geometry + metrics",
            description = "Proxy to external routing service (OSRM). Returns GeoJSON geometry plus distance/duration. " +
                          "Uses a safe fallback if the routing service is unavailable.")
    @ApiResponse(responseCode = "200", description = "Route computed")
    public RouteResponse getRoute(
            @RequestParam @NotNull @Min(-90) @Max(90) Double fromLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double fromLon,
            @RequestParam @NotNull @Min(-90) @Max(90) Double toLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double toLon) {

        RoutingProxyService.RouteWithGeometry route = routingProxyService.getRoute(fromLat, fromLon, toLat, toLon);
        return new RouteResponse(route.distanceKm(), route.durationMinutes(), route.geometry(), route.fallback());
    }

    public record RouteResponse(
            @Schema(example = "2.35") BigDecimal distanceKm,
            @Schema(example = "9") Integer durationMinutes,
            RoutingProxyService.GeoJsonLineString geometry,
            @Schema(description = "True when OSRM was unavailable and we returned an approximate straight-line route") boolean fallback
    ) {}
}
