package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.CreateStoreRequest;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.core.service.model.UpdateStoreRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Store management.
 */
@RestController
@RequestMapping(value = "/api/v1/stores", produces = MediaType.APPLICATION_JSON_VALUE)
public class StoreResource {

    private final StoreService storeService;

    public StoreResource(final StoreService storeService) {
        if (storeService == null) throw new NullPointerException();
        this.storeService = storeService;
    }

    @GetMapping
    public List<StoreView> getAllStores(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) CuisineType cuisine,
            @RequestParam(required = false) String search) {

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

    @GetMapping("/{id}")
    public ResponseEntity<StoreView> getStore(@PathVariable Long id) {
        return this.storeService.getStore(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<StoreView> createStore(@RequestBody @Valid CreateStoreRequest request) {
        final StoreView created = this.storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<StoreView> updateStore(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStoreRequest request) {

        final StoreView updated = this.storeService.updateStore(id, request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStoreStatus(
            @PathVariable Long id,
            @RequestParam boolean isOpen) {

        this.storeService.toggleStoreStatus(id, isOpen);
        return ResponseEntity.noContent().build();
    }
}