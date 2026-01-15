package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.service.FavoriteStoreService;
import com.streetfoodgo.core.service.model.StoreView;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing favorite stores.
 */
@RestController
@RequestMapping(value = "/api/v1/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
public class FavoriteStoreResource {

    private final FavoriteStoreService favoriteStoreService;

    public FavoriteStoreResource(final FavoriteStoreService favoriteStoreService) {
        if (favoriteStoreService == null) throw new NullPointerException();
        this.favoriteStoreService = favoriteStoreService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/customer/{customerId}/store/{storeId}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable Long customerId,
            @PathVariable Long storeId) {

        favoriteStoreService.addFavorite(customerId, storeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/customer/{customerId}/store/{storeId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long customerId,
            @PathVariable Long storeId) {

        favoriteStoreService.removeFavorite(customerId, storeId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer/{customerId}/store/{storeId}")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Long customerId,
            @PathVariable Long storeId) {

        boolean isFavorite = favoriteStoreService.isFavorite(customerId, storeId);
        return ResponseEntity.ok(isFavorite);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public List<StoreView> getFavoriteStores(@PathVariable Long customerId) {
        return favoriteStoreService.getFavoriteStores(customerId);
    }

    @GetMapping("/store/{storeId}/count")
    public ResponseEntity<Long> getFavoriteCount(@PathVariable Long storeId) {
        Long count = favoriteStoreService.getFavoriteCount(storeId);
        return ResponseEntity.ok(count);
    }
}
