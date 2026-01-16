package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.model.CreateMenuItemRequest;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.UpdateMenuItemRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for MenuItem management.
 */
@RestController
@RequestMapping(value = "/api/v1/menu-items", produces = MediaType.APPLICATION_JSON_VALUE)
public class MenuItemResource {

    private final MenuItemService menuItemService;

    public MenuItemResource(final MenuItemService menuItemService) {
        if (menuItemService == null) throw new NullPointerException();
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public List<MenuItemView> getMenuItems(
            @RequestParam Long storeId,
            @RequestParam(required = false) MenuCategory category,
            @RequestParam(defaultValue = "true") boolean availableOnly) {

        if (category != null) {
            return this.menuItemService.getStoreMenuByCategory(storeId, category);
        }
        if (availableOnly) {
            return this.menuItemService.getAvailableStoreMenu(storeId);
        }
        return this.menuItemService.getStoreMenu(storeId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemView> getMenuItem(@PathVariable Long id) {
        return this.menuItemService.getMenuItem(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<MenuItemView> createMenuItem(@RequestBody @Valid CreateMenuItemRequest request) {
        final MenuItemView created = this.menuItemService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemView> updateMenuItem(
            @PathVariable Long id,
            @RequestBody @Valid UpdateMenuItemRequest request) {

        final MenuItemView updated = this.menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Void> toggleAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {

        this.menuItemService.toggleAvailability(id, available);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        this.menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}