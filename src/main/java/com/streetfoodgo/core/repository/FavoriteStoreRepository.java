package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.FavoriteStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FavoriteStore entity.
 */
@Repository
public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {

    List<FavoriteStore> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<FavoriteStore> findByCustomerIdAndStoreId(Long customerId, Long storeId);

    boolean existsByCustomerIdAndStoreId(Long customerId, Long storeId);

    void deleteByCustomerIdAndStoreId(Long customerId, Long storeId);

    @Query("SELECT COUNT(f) FROM FavoriteStore f WHERE f.store.id = :storeId")
    Long countFavoritesForStore(Long storeId);
}
