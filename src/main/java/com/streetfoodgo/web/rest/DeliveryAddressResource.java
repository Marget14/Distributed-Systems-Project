package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.service.DeliveryAddressService;
import com.streetfoodgo.core.service.model.CreateDeliveryAddressRequest;
import com.streetfoodgo.core.service.model.DeliveryAddressView;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for DeliveryAddress management.
 */
@RestController
@RequestMapping(value = "/api/v1/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('CUSTOMER')")
public class DeliveryAddressResource {

    private final DeliveryAddressService deliveryAddressService;

    public DeliveryAddressResource(final DeliveryAddressService deliveryAddressService) {
        if (deliveryAddressService == null) throw new NullPointerException();
        this.deliveryAddressService = deliveryAddressService;
    }

    @GetMapping
    public List<DeliveryAddressView> getMyAddresses(@RequestParam Long customerId) {
        return this.deliveryAddressService.getCustomerAddresses(customerId);
    }

    @GetMapping("/default")
    public ResponseEntity<DeliveryAddressView> getDefaultAddress(@RequestParam Long customerId) {
        return this.deliveryAddressService.getDefaultAddress(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryAddressView> getAddress(@PathVariable Long id) {
        return this.deliveryAddressService.getAddress(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DeliveryAddressView> createAddress(
            @RequestBody @Valid CreateDeliveryAddressRequest request) {

        final DeliveryAddressView created = this.deliveryAddressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryAddressView> updateAddress(
            @PathVariable Long id,
            @RequestBody @Valid CreateDeliveryAddressRequest request) {

        final DeliveryAddressView updated = this.deliveryAddressService.updateAddress(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/set-default")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable Long id) {
        this.deliveryAddressService.setDefaultAddress(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        this.deliveryAddressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}