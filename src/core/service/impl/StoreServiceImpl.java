package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.model.PersonType;
import com.streetfoodgo.core.model.Store;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.repository.StoreRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.StoreService;
import com.streetfoodgo.core.service.mapper.StoreMapper;
import com.streetfoodgo.core.service.model.CreateStoreRequest;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.core.service.model.UpdateStoreRequest;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of StoreService.
 */
@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final PersonRepository personRepository;
    private final StoreMapper storeMapper;
    private final CurrentUserProvider currentUserProvider;

    public StoreServiceImpl(
            final StoreRepository storeRepository,
            final PersonRepository personRepository,
            final StoreMapper storeMapper,
            final CurrentUserProvider currentUserProvider) {

        if (storeRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (storeMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.storeRepository = storeRepository;
        this.personRepository = personRepository;
        this.storeMapper = storeMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<StoreView> getAllStores() {
        return this.storeRepository.findAll()
                .stream()
                .map(this.storeMapper::toView)
                .toList();
    }

    @Override
    public List<StoreView> getOpenStores() {
        return this.storeRepository.findAllByIsOpenTrue()
                .stream()
                .map(this.storeMapper::toView)
                .toList();
    }

    @Override
    public List<StoreView> getStoresByArea(final String area) {
        if (area == null || area.isBlank()) {
            return getAllStores();
        }
        return this.storeRepository.findAllByAreaIgnoreCase(area)
                .stream()
                .map(this.storeMapper::toView)
                .toList();
    }

    @Override
    public List<StoreView> getStoresByCuisine(final CuisineType cuisineType) {
        if (cuisineType == null) {
            return getAllStores();
        }
        return this.storeRepository.findAllByCuisineTypeAndIsOpenTrue(cuisineType)
                .stream()
                .map(this.storeMapper::toView)
                .toList();
    }

    @Override
    public List<StoreView> searchStores(final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllStores();
        }
        return this.storeRepository.searchByKeyword(keyword)
                .stream()
                .map(this.storeMapper::toView)
                .toList();
    }

    @Override
    public Optional<StoreView> getStore(final Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        return this.storeRepository.findById(id)
                .map(this.storeMapper::toView);
    }

    @Override
    public List<StoreView> getOwnerStores(final Long ownerId) {
        if (ownerId == null || ownerId <= 0) throw new IllegalArgumentException();

        return this.storeRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(this.storeMapper::toView)
                .toList();
    }

    @Transactional
    @Override
    public StoreView createStore(final CreateStoreRequest request) {
        if (request == null) throw new NullPointerException();

        // Get current user (must be OWNER)
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.OWNER) {
            throw new SecurityException("Only OWNER can create stores");
        }

        final Person owner = this.personRepository.findById(currentUser.id())
                .orElseThrow(() -> new IllegalStateException("Owner not found"));

        // Create Store entity
        Store store = new Store();
        store.setOwner(owner);
        store.setName(request.name());
        store.setDescription(request.description());
        store.setCuisineType(request.cuisineType());
        store.setStoreType(request.storeType());
        store.setAddress(request.address());
        store.setLatitude(request.latitude());
        store.setLongitude(request.longitude());
        store.setArea(request.area());
        store.setOpeningHours(request.openingHours());
        store.setIsOpen(true);
        store.setMinimumOrderAmount(request.minimumOrderAmount() != null ? request.minimumOrderAmount() : BigDecimal.ZERO);
        store.setAcceptsDelivery(request.acceptsDelivery() != null ? request.acceptsDelivery() : true);
        store.setAcceptsPickup(request.acceptsPickup() != null ? request.acceptsPickup() : true);
        store.setDeliveryFee(request.deliveryFee() != null ? request.deliveryFee() : BigDecimal.valueOf(2.50));
        store.setEstimatedDeliveryTimeMinutes(request.estimatedDeliveryTimeMinutes() != null ? request.estimatedDeliveryTimeMinutes() : 30);
        store.setImageUrl(request.imageUrl());

        store = this.storeRepository.save(store);

        return this.storeMapper.toView(store);
    }

    @Transactional
    @Override
    public StoreView updateStore(final Long id, final UpdateStoreRequest request) {
        if (id == null || id <= 0) throw new IllegalArgumentException();
        if (request == null) throw new NullPointerException();

        Store store = this.storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Security: Only owner can update their store
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!store.getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can update the store");
        }

        // Update fields if provided
        if (request.name() != null) store.setName(request.name());
        if (request.description() != null) store.setDescription(request.description());
        if (request.openingHours() != null) store.setOpeningHours(request.openingHours());
        if (request.isOpen() != null) store.setIsOpen(request.isOpen());
        if (request.minimumOrderAmount() != null) store.setMinimumOrderAmount(request.minimumOrderAmount());
        if (request.acceptsDelivery() != null) store.setAcceptsDelivery(request.acceptsDelivery());
        if (request.acceptsPickup() != null) store.setAcceptsPickup(request.acceptsPickup());
        if (request.deliveryFee() != null) store.setDeliveryFee(request.deliveryFee());
        if (request.estimatedDeliveryTimeMinutes() != null) store.setEstimatedDeliveryTimeMinutes(request.estimatedDeliveryTimeMinutes());
        if (request.imageUrl() != null) store.setImageUrl(request.imageUrl());

        store = this.storeRepository.save(store);

        return this.storeMapper.toView(store);
    }

    @Transactional
    @Override
    public void toggleStoreStatus(final Long id, final boolean isOpen) {
        if (id == null || id <= 0) throw new IllegalArgumentException();

        Store store = this.storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Security: Only owner can toggle status
        final var currentUser = this.currentUserProvider.requireCurrentUser();
        if (!store.getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can toggle status");
        }

        store.setIsOpen(isOpen);
        this.storeRepository.save(store);
    }
}