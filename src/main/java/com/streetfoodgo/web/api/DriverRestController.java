package com.streetfoodgo.web.api;

import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.model.UpdateDriverLocationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Driver API", description = "Endpoints for driver apps")
public class DriverRestController {

    private final OrderService orderService;

    public DriverRestController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{id}/location")
    @Operation(summary = "Update driver location", description = "Called by the driver app to update real-time coordinates")
    public ResponseEntity<Void> updateDriverLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDriverLocationRequest request) {

        this.orderService.updateDriverLocation(id, request.latitude(), request.longitude());
        return ResponseEntity.ok().build();
    }
}
