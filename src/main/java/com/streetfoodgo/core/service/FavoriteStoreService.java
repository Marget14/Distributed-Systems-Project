package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.StoreView;

import java.util.List;

/**
 * Service for managing customer's favorite stores.
 */
public interface FavoriteStoreService {

    /**
     * Add a store to customer's favorites.
     *
     * @param customerId Customer ID
     * @param storeId Store ID
     */
    void addFavorite(Long customerId, Long storeId);

    /**
     * Remove a store from customer's favorites.
     *
     * @param customerId Customer ID
     * @param storeId Store ID
     */
    void removeFavorite(Long customerId, Long storeId);

    /**
     * Check if a store is in customer's favorites.
     *
     * @param customerId Customer ID
     * @param storeId Store ID
     * @return true if store is favorited
     */
    boolean isFavorite(Long customerId, Long storeId);

    /**
     * Get all favorite stores for a customer.
     *
     * @param customerId Customer ID
     * @return List of favorite stores
     */
    List<StoreView> getFavoriteStores(Long customerId);

    /**
     * Get count of customers who favorited a store.
     *
     * @param storeId Store ID
     * @return Number of favorites
     */
    Long getFavoriteCount(Long storeId);
}
