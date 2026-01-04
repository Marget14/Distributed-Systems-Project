package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.service.model.CreateMenuItemRequest;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.UpdateMenuItemRequest;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing MenuItem business logic.
 */
public interface MenuItemService {

    List<MenuItemView> getStoreMenu(Long storeId);

    List<MenuItemView> getAvailableStoreMenu(Long storeId);

    List<MenuItemView> getStoreMenuByCategory(Long storeId, MenuCategory category);

    Optional<MenuItemView> getMenuItem(Long id);

    MenuItemView createMenuItem(CreateMenuItemRequest request);

    MenuItemView updateMenuItem(Long id, UpdateMenuItemRequest request);

    void toggleAvailability(Long id, boolean available);

    void deleteMenuItem(Long id);
}