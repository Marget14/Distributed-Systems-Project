package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.CreateStoreRequest;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.core.service.model.UpdateStoreRequest;
import com.streetfoodgo.core.service.GeolocationService;
import com.streetfoodgo.core.service.model.DeliveryAddressView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Store management.
 * Provides endpoints for querying stores and managing them (for store owners).
 */
@RestController
@RequestMapping(value = "/api/v1/stores", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Stores", description = "APIs for browsing and managing food stores/restaurants")
public class StoreResource {

    private final StoreService storeService;
    private final GeolocationService geolocationService;

    public StoreResource(final StoreService storeService, final GeolocationService geolocationService) {
        if (storeService == null) throw new NullPointerException();
        if (geolocationService == null) throw new NullPointerException();
        this.storeService = storeService;
        this.geolocationService = geolocationService;
    }

    /**
     * Get all stores, optionally filtered by area, cuisine type, or search keyword.
     */
    @GetMapping
    @Operation(summary = "Get stores",
               description = "Retrieve stores with optional filtering by area, cuisine type, or search keyword. If no filters provided, returns only open stores.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved stores"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public List<StoreView> getAllStores(
            @Parameter(description = "Filter by area/location") @RequestParam(required = false) String area,
            @Parameter(description = "Filter by cuisine type") @RequestParam(required = false) CuisineType cuisine,
            @Parameter(description = "Search by store name or description") @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            return this.storeService.searchStores(search);
        }
        if (cuisine != null) {
            return this.storeService.getStoresByCuisine(cuisine);
        }
        if (area != null && !area.isBlank()) {
            return this.storeService.getStoresByArea(area);
        }
        return this.storeService.getOpenStores();
    }

    /**
     * Get a specific store by ID with full details.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get store details",
               description = "Retrieve detailed information about a specific store including menu, hours, and delivery options.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Store found"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<StoreView> getStore(
            @Parameter(description = "Store ID") @PathVariable Long id) {
        return this.storeService.getStore(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new store (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create store",
               description = "Create a new store. Requires OWNER role and valid JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Store created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not an OWNER")
    })
    public ResponseEntity<StoreView> createStore(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Store creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateStoreRequest.class)))
            @RequestBody @Valid CreateStoreRequest request) {
        final StoreView created = this.storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing store (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update store",
               description = "Update details of an existing store. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Store updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<StoreView> updateStore(
            @Parameter(description = "Store ID") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Store update data",
                    required = true)
            @RequestBody @Valid UpdateStoreRequest request) {

        final StoreView updated = this.storeService.updateStore(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Toggle store open/closed status (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle store status",
               description = "Open or close a store. When closed, customers cannot place orders. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Store status updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<Void> toggleStoreStatus(
            @Parameter(description = "Store ID") @PathVariable Long id,
            @Parameter(description = "Desired status (true=open, false=closed)") @RequestParam boolean isOpen) {

        this.storeService.toggleStoreStatus(id, isOpen);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get nearby stores ordered by estimated delivery time.
     */
    @GetMapping("/nearby")
    @Operation(summary = "Get nearby stores",
            description = "Return open stores ordered by estimated delivery time from the given coordinates. Uses an external geolocation service (OSRM) when available.")
    public List<NearbyStoreView> getNearbyStores(
            @Parameter(description = "Customer latitude") @RequestParam double lat,
            @Parameter(description = "Customer longitude") @RequestParam double lon,
            @Parameter(description = "Max stores to return (optional)") @RequestParam(required = false, defaultValue = "30") int limit) {

        DeliveryAddressView address = new DeliveryAddressView(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                lat,
                lon,
                false,
                null
        );

        return this.geolocationService.rankStoresByDeliveryTime(this.storeService.getOpenStores(), address, limit)
                .stream()
                .map(swm -> new NearbyStoreView(
                        swm.store(),
                        swm.metrics().distanceKm(),
                        swm.metrics().estimatedMinutes()
                ))
                .toList();
    }

    public record NearbyStoreView(
            StoreView store,
            BigDecimal distanceKm,
            Integer estimatedMinutes
    ) {}
}