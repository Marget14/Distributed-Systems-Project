package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.FavoriteStore;
import com.streetfoodgo.core.model.Person;
import com.streetfoodgo.core.model.PersonType;
import com.streetfoodgo.core.model.Store;
import com.streetfoodgo.core.repository.FavoriteStoreRepository;
import com.streetfoodgo.core.repository.PersonRepository;
import com.streetfoodgo.core.repository.StoreRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.FavoriteStoreService;
import com.streetfoodgo.core.service.mapper.StoreMapper;
import com.streetfoodgo.core.service.model.StoreView;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of FavoriteStoreService.
 */
@Service
public class FavoriteStoreServiceImpl implements FavoriteStoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FavoriteStoreServiceImpl.class);

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final PersonRepository personRepository;
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final CurrentUserProvider currentUserProvider;

    public FavoriteStoreServiceImpl(
            final FavoriteStoreRepository favoriteStoreRepository,
            final PersonRepository personRepository,
            final StoreRepository storeRepository,
            final StoreMapper storeMapper,
            final CurrentUserProvider currentUserProvider) {

        if (favoriteStoreRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (storeRepository == null) throw new NullPointerException();
        if (storeMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.favoriteStoreRepository = favoriteStoreRepository;
        this.personRepository = personRepository;
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    @Override
    public void addFavorite(final Long customerId, final Long storeId) {
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (storeId == null) throw new IllegalArgumentException("Store ID cannot be null");

        // Security check
        final var currentUser = currentUserProvider.requireCurrentUser();
        if (!Long.valueOf(currentUser.id()).equals(customerId)) {
            throw new SecurityException("Cannot manage favorites for another customer");
        }

        // Check if already favorited
        if (favoriteStoreRepository.existsByCustomerIdAndStoreId(customerId, storeId)) {
            LOGGER.debug("Store {} already in favorites for customer {}", storeId, customerId);
            return; // Already favorited, nothing to do
        }

        // Load entities
        Person customer = personRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.getType() != PersonType.CUSTOMER) {
            throw new IllegalArgumentException("Only customers can favorite stores");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Create favorite
        FavoriteStore favorite = new FavoriteStore(customer, store);
        favoriteStoreRepository.save(favorite);

        LOGGER.info("Store {} added to favorites for customer {}", storeId, customerId);
    }

    @Transactional
    @Override
    public void removeFavorite(final Long customerId, final Long storeId) {
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (storeId == null) throw new IllegalArgumentException("Store ID cannot be null");

        // Security check
        final var currentUser = currentUserProvider.requireCurrentUser();
        if (!Long.valueOf(currentUser.id()).equals(customerId)) {
            throw new SecurityException("Cannot manage favorites for another customer");
        }

        favoriteStoreRepository.deleteByCustomerIdAndStoreId(customerId, storeId);

        LOGGER.info("Store {} removed from favorites for customer {}", storeId, customerId);
    }

    @Override
    public boolean isFavorite(final Long customerId, final Long storeId) {
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");
        if (storeId == null) throw new IllegalArgumentException("Store ID cannot be null");

        return favoriteStoreRepository.existsByCustomerIdAndStoreId(customerId, storeId);
    }

    @Override
    public List<StoreView> getFavoriteStores(final Long customerId) {
        if (customerId == null) throw new IllegalArgumentException("Customer ID cannot be null");

        // Security check
        final var currentUser = currentUserProvider.requireCurrentUser();
        if (!Long.valueOf(currentUser.id()).equals(customerId) && currentUser.type() != PersonType.ADMIN) {
            throw new SecurityException("Cannot view favorites for another customer");
        }

        return favoriteStoreRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(FavoriteStore::getStore)
                .map(storeMapper::toView)
                .collect(Collectors.toList());
    }

    @Override
    public Long getFavoriteCount(final Long storeId) {
        if (storeId == null) throw new IllegalArgumentException("Store ID cannot be null");

        return favoriteStoreRepository.countFavoritesForStore(storeId);
    }
}
