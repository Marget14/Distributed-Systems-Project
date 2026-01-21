package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.model.CreateMenuItemRequest;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.UpdateMenuItemRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for MenuItem management.
 * Provides endpoints for querying menu items and managing them (for store owners).
 */
@RestController
@RequestMapping(value = "/api/v1/menu-items", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Menu Items", description = "APIs for managing menu items within stores")
public class MenuItemResource {

    private final MenuItemService menuItemService;

    public MenuItemResource(final MenuItemService menuItemService) {
        if (menuItemService == null) throw new NullPointerException();
        this.menuItemService = menuItemService;
    }

    /**
     * Get menu items for a store, optionally filtered by category or availability.
     */
    @GetMapping
    @Operation(summary = "Get store menu items",
               description = "Retrieve menu items for a specific store, optionally filtered by category and availability status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved menu items"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters (missing storeId or invalid category)")
    })
    public List<MenuItemView> getMenuItems(
            @Parameter(description = "Store ID", required = true) @RequestParam Long storeId,
            @Parameter(description = "Optional menu category filter") @RequestParam(required = false) MenuCategory category,
            @Parameter(description = "Return only available items (default: true)") @RequestParam(defaultValue = "true") boolean availableOnly) {

        if (category != null) {
            return this.menuItemService.getStoreMenuByCategory(storeId, category);
        }
        if (availableOnly) {
            return this.menuItemService.getAvailableStoreMenu(storeId);
        }
        return this.menuItemService.getStoreMenu(storeId);
    }

    /**
     * Get a specific menu item by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get menu item details",
               description = "Retrieve detailed information about a specific menu item.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Menu item found"),
        @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    public ResponseEntity<MenuItemView> getMenuItem(
            @Parameter(description = "Menu item ID") @PathVariable Long id) {
        return this.menuItemService.getMenuItem(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new menu item (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    @Operation(summary = "Create menu item",
               description = "Create a new menu item for your store. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data (validation failed)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not an OWNER")
    })
    public ResponseEntity<MenuItemView> createMenuItem(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Menu item creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateMenuItemRequest.class)))
            @RequestBody @Valid CreateMenuItemRequest request) {
        final MenuItemView created = this.menuItemService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing menu item (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update menu item",
               description = "Update details of an existing menu item. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    public ResponseEntity<MenuItemView> updateMenuItem(
            @Parameter(description = "Menu item ID") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Menu item update data",
                    required = true)
            @RequestBody @Valid UpdateMenuItemRequest request) {

        final MenuItemView updated = this.menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Toggle availability of a menu item (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{id}/availability")
    @Operation(summary = "Toggle menu item availability",
               description = "Enable or disable a menu item. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Availability toggled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    public ResponseEntity<Void> toggleAvailability(
            @Parameter(description = "Menu item ID") @PathVariable Long id,
            @Parameter(description = "Availability flag (true/false)") @RequestParam boolean available) {

        this.menuItemService.toggleAvailability(id, available);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a menu item (OWNER role required).
     */
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete menu item",
               description = "Permanently delete a menu item. Requires OWNER role.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Menu item deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    public ResponseEntity<Void> deleteMenuItem(
            @Parameter(description = "Menu item ID") @PathVariable Long id) {
        this.menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}