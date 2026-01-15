package com.streetfoodgo.core.service;

import com.streetfoodgo.core.service.model.CreateReviewRequest;
import com.streetfoodgo.core.service.model.ReviewView;
import com.streetfoodgo.core.service.model.StoreRatingsSummary;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing reviews and ratings.
 */
public interface ReviewService {

    /**
     * Create a new review.
     */
    ReviewView createReview(CreateReviewRequest request);

    /**
     * Get all reviews for a store.
     */
    List<ReviewView> getStoreReviews(Long storeId);

    /**
     * Get reviews by customer.
     */
    List<ReviewView> getCustomerReviews(Long customerId);

    /**
     * Get review by order.
     */
    Optional<ReviewView> getReviewByOrder(Long orderId);

    /**
     * Check if order has been reviewed.
     */
    boolean hasOrderBeenReviewed(Long orderId);

    /**
     * Get store ratings summary.
     */
    StoreRatingsSummary getStoreRatingsSummary(Long storeId);

    /**
     * Add owner response to a review.
     */
    ReviewView addOwnerResponse(Long reviewId, String response);

    /**
     * Get a single review by ID.
     */
    Optional<ReviewView> getReview(Long reviewId);
}
