package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.model.CreateDeliveryAddressRequest;
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

import java.util.List;

/**
 * REST controller for DeliveryAddress management.
 * Requires CUSTOMER role for all operations.
 */
@RestController
@RequestMapping(value = "/api/v1/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('CUSTOMER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Delivery Addresses", description = "APIs for managing customer delivery addresses")
public class DeliveryAddressResource {

    private final DeliveryAddressService deliveryAddressService;

    public DeliveryAddressResource(final DeliveryAddressService deliveryAddressService) {
        if (deliveryAddressService == null) throw new NullPointerException();
        this.deliveryAddressService = deliveryAddressService;
    }

    /**
     * Get all delivery addresses for the authenticated customer.
     */
    @GetMapping
    @Operation(summary = "Get my addresses",
               description = "Retrieve all delivery addresses for the current authenticated customer.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved addresses"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not a customer")
    })
    public List<DeliveryAddressView> getMyAddresses(
            final org.springframework.security.core.Authentication authentication) {

        final long customerId = RestSecurityUtils.requireUserId(authentication);
        return this.deliveryAddressService.getCustomerAddresses(customerId);
    }

    /**
     * Get the default delivery address for the authenticated customer.
     */
    @GetMapping("/default")
    @Operation(summary = "Get default address",
               description = "Retrieve the default delivery address for the current customer.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Default address found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "No default address set")
    })
    public ResponseEntity<DeliveryAddressView> getDefaultAddress(
            final org.springframework.security.core.Authentication authentication) {

        final long customerId = RestSecurityUtils.requireUserId(authentication);
        return this.deliveryAddressService.getDefaultAddress(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a specific delivery address by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get address details",
               description = "Retrieve detailed information about a specific delivery address.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Address found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<DeliveryAddressView> getAddress(
            @Parameter(description = "Address ID") @PathVariable Long id) {
        return this.deliveryAddressService.getAddress(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new delivery address for the current customer.
     */
    @PostMapping
    @Operation(summary = "Create new address",
               description = "Add a new delivery address for the current customer.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Address created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DeliveryAddressView> createAddress(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Address creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateDeliveryAddressRequest.class)))
            @RequestBody @Valid CreateDeliveryAddressRequest request) {

        final DeliveryAddressView created = this.deliveryAddressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing delivery address.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update address",
               description = "Update details of an existing delivery address.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Address updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<DeliveryAddressView> updateAddress(
            @Parameter(description = "Address ID") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Address update data",
                    required = true)
            @RequestBody @Valid CreateDeliveryAddressRequest request) {

        final DeliveryAddressView updated = this.deliveryAddressService.updateAddress(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Set a delivery address as the default.
     */
    @PatchMapping("/{id}/set-default")
    @Operation(summary = "Set default address",
               description = "Mark a delivery address as the default for the current customer.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Default address set successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<Void> setDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable Long id) {
        this.deliveryAddressService.setDefaultAddress(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a delivery address.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address",
               description = "Remove a delivery address from the customer's account.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable Long id) {
        this.deliveryAddressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}