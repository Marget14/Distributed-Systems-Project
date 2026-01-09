package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.model.MenuItem;
import com.streetfoodgo.core.model.Store;
import com.streetfoodgo.core.repository.MenuItemRepository;
import com.streetfoodgo.core.repository.StoreRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.MenuItemService;
import com.streetfoodgo.core.service.mapper.MenuItemMapper;
import com.streetfoodgo.core.service.model.CreateMenuItemRequest;
import com.streetfoodgo.core.service.model.MenuItemView;
import com.streetfoodgo.core.service.model.UpdateMenuItemRequest;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of MenuItemService.
 */
@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final StoreRepository storeRepository;
    private final MenuItemMapper menuItemMapper;
    private final CurrentUserProvider currentUserProvider;

    public MenuItemServiceImpl(
            final MenuItemRepository menuItemRepository,
            final StoreRepository storeRepository,
            final MenuItemMapper menuItemMapper,
            final CurrentUserProvider currentUserProvider) {

        if (menuItemRepository == null) throw new NullPointerException();
        if (storeRepository == null) throw new NullPointerException();
        if (menuItemMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.menuItemRepository = menuItemRepository;
        this.storeRepository = storeRepository;
        this.menuItemMapper = menuItemMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<MenuItemView> getStoreMenu(final Long storeId) {
        if (storeId == null || storeId <= 0) throw new IllegalArgumentException();

        return this.menuItemRepository.findAllByStoreId(storeId)
                .stream()
                .map(this.menuItemMapper::toView)
                .toList();
    }

    @Override
    public List<MenuItemView> getAvailableStoreMenu(final Long storeId) {
        if (storeId == null || storeId <= 0) throw new IllegalArgumentException();

        return this.menuItemRepository.findAllByStoreIdAndAvailableTrue(storeId)
                .stream()
                .map(this.menuItemMapper::toView)
                .toList();
    }

    @Override
    public List<MenuItemView> getStoreMenuByCategory(final Long storeId, final MenuCategory category) {
        if (storeId == null || storeId <= 0) throw new IllegalArgumentException();
        if (category == null) return getAvailableStoreMenu(storeId);

        return this.menuItemRepository.findAllByStoreIdAndCategoryAndAvailableTrue(storeId, category)
                .stream()
                .map(this.menuItemMapper::toView)
                .toList();
    }

    @Override
    public Optional<MenuItemView> getMenuItem(final Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        return this.menuItemRepository.findById(id)
                .map(this.menuItemMapper::toView);
    }

    @Transactional
    @Override
    public MenuItemView createMenuItem(final CreateMenuItemRequest request) {
        if (request == null) throw new NullPointerException();

        final Store store = this.storeRepository.findById(request.storeId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Security: Only store owner can create menu items
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!store.getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can create menu items");
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setStore(store);
        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setCategory(request.category());
        menuItem.setAvailable(true);
        menuItem.setImageUrl(request.imageUrl());

        menuItem = this.menuItemRepository.save(menuItem);

        return this.menuItemMapper.toView(menuItem);
    }

    @Transactional
    @Override
    public MenuItemView updateMenuItem(final Long id, final UpdateMenuItemRequest request) {
        if (id == null || id <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        MenuItem menuItem = this.menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        // Security: Only store owner can update menu items
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!menuItem.getStore().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can update menu items");
        }

        if (request.name() != null) menuItem.setName(request.name());
        if (request.description() != null) menuItem.setDescription(request.description());
        if (request.price() != null) menuItem.setPrice(request.price());
        if (request.available() != null) menuItem.setAvailable(request.available());
        if (request.imageUrl() != null) menuItem.setImageUrl(request.imageUrl());

        menuItem = this.menuItemRepository.save(menuItem);

        return this.menuItemMapper.toView(menuItem);
    }

    @Transactional
    @Override
    public void toggleAvailability(final Long id, final boolean available) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        MenuItem menuItem = this.menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        // Security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!menuItem.getStore().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can toggle availability");
        }

        menuItem.setAvailable(available);
        this.menuItemRepository.save(menuItem);
    }

    @Transactional
    @Override
    public void deleteMenuItem(final Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        MenuItem menuItem = this.menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        // Security check
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!menuItem.getStore().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can delete menu items");
        }

        this.menuItemRepository.delete(menuItem);
    }
}