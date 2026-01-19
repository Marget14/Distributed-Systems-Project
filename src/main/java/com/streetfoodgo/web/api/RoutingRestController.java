package com.streetfoodgo.web.api;

import com.streetfoodgo.core.service.RoutingProxyService;
import com.streetfoodgo.core.service.RoutingProxyService.RouteWithGeometry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routing")
@Tag(name = "Routing API", description = "Endpoints for route calculation and delivery estimation")
public class RoutingRestController {

    private final RoutingProxyService routingProxyService;

    public RoutingRestController(final RoutingProxyService routingProxyService) {
        if (routingProxyService == null) throw new NullPointerException("routingProxyService cannot be null");
        this.routingProxyService = routingProxyService;
    }

    @GetMapping("/route")
    @Operation(summary = "Calculate route between two points", description = "Returns distance, duration, and route geometry (GeoJSON)")
    public ResponseEntity<RouteWithGeometry> getRoute(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon) {

        RouteWithGeometry route = this.routingProxyService.getRoute(fromLat, fromLon, toLat, toLon);
        return ResponseEntity.ok(route);
    }
}
