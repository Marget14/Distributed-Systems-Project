package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review entity.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    Page<Review> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<Review> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.id = :storeId")
    Double calculateAverageRatingForStore(Long storeId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.store.id = :storeId")
    Long countReviewsForStore(Long storeId);

    @Query("SELECT AVG(r.foodRating) FROM Review r WHERE r.store.id = :storeId AND r.foodRating IS NOT NULL")
    Double calculateAverageFoodRating(Long storeId);

    @Query("SELECT AVG(r.deliveryRating) FROM Review r WHERE r.store.id = :storeId AND r.deliveryRating IS NOT NULL")
    Double calculateAverageDeliveryRating(Long storeId);
}
