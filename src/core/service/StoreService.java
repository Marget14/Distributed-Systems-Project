package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.service.model.CreateStoreRequest;
import com.streetfoodgo.core.service.model.StoreView;
import com.streetfoodgo.core.service.model.UpdateStoreRequest;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Store business logic.
 */
public interface StoreService {

    List<StoreView> getAllStores();

    List<StoreView> getOpenStores();

    List<StoreView> getStoresByArea(String area);

    List<StoreView> getStoresByCuisine(CuisineType cuisineType);

    List<StoreView> searchStores(String keyword);

    Optional<StoreView> getStore(Long id);

    List<StoreView> getOwnerStores(Long ownerId);

    StoreView createStore(CreateStoreRequest request);

    StoreView updateStore(Long id, UpdateStoreRequest request);

    void toggleStoreStatus(Long id, boolean isOpen);
}