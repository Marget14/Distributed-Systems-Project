package com.streetfoodgo.web.ui;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.StoreView;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for browsing stores and menus.
 */
@Controller
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;
    private final MenuItemService menuItemService;

    public StoreController(
            final StoreService storeService,
            final MenuItemService menuItemService) {

        if (storeService == null) throw new NullPointerException();
        if (menuItemService == null) throw new NullPointerException();

        this.storeService = storeService;
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public String listStores(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String search,
            final Model model) {

        List<StoreView> stores;

        if (search != null && !search.isBlank()) {
            stores = this.storeService.searchStores(search);
            model.addAttribute("searchQuery", search);
        } else if (cuisine != null && !cuisine.isBlank()) {
            try {
                CuisineType cuisineType = CuisineType.valueOf(cuisine);
                stores = this.storeService.getStoresByCuisine(cuisineType);
                model.addAttribute("selectedCuisine", cuisine);
            } catch (IllegalArgumentException e) {
                stores = this.storeService.getOpenStores();
            }
        } else if (area != null && !area.isBlank()) {
            stores = this.storeService.getStoresByArea(area);
            model.addAttribute("selectedArea", area);
        } else {
            stores = this.storeService.getOpenStores();
        }

        model.addAttribute("stores", stores);
        model.addAttribute("cuisineTypes", CuisineType.values());

        return "stores/list";
    }

    @GetMapping("/{id}")
    public String viewStore(@PathVariable Long id, final Model model) {
        final StoreView store = this.storeService.getStore(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        final List<MenuItemView> menuItems = this.menuItemService.getAvailableStoreMenu(id);

        // Extract unique categories from menu items (convert enum to string)
        final List<String> categories = menuItems.stream()
                .map(item -> item.category().name())
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("store", store);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("categories", categories);

        return "stores/detail";
    }
}