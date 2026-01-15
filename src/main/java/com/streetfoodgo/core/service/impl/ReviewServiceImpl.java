package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.*;
import com.streetfoodgo.core.repository.OrderRepository;
import com.streetfoodgo.core.repository.ReviewRepository;
import com.streetfoodgo.core.repository.StoreRepository;
import com.streetfoodgo.core.security.CurrentUserProvider;
import com.streetfoodgo.core.service.ReviewService;
import com.streetfoodgo.core.service.model.CreateReviewRequest;
import com.streetfoodgo.core.service.model.ReviewView;
import com.streetfoodgo.core.service.model.StoreRatingsSummary;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ReviewService.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReviewServiceImpl(
            final ReviewRepository reviewRepository,
            final OrderRepository orderRepository,
            final StoreRepository storeRepository,
            final CurrentUserProvider currentUserProvider) {

        if (reviewRepository == null) throw new NullPointerException();
        if (orderRepository == null) throw new NullPointerException();
        if (storeRepository == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    @Override
    public ReviewView createReview(final CreateReviewRequest request) {
        if (request == null) throw new NullPointerException();

        // Get current user
        final var currentUser = currentUserProvider.requireCurrentUser();

        // Load order
        final Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Security: Only customer who made the order can review it
        if (!order.getCustomer().getId().equals(Long.valueOf(currentUser.id()))) {
            throw new SecurityException("Can only review your own orders");
        }

        // Validate order is completed
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Can only review completed orders");
        }

        // Check if already reviewed
        if (reviewRepository.existsByOrderId(request.orderId())) {
            throw new IllegalArgumentException("Order has already been reviewed");
        }

        // Create review
        Review review = new Review();
        review.setOrder(order);
        review.setStore(order.getStore());
        review.setCustomer(order.getCustomer());
        review.setRating(request.rating());
        review.setFoodRating(request.foodRating());
        review.setDeliveryRating(request.deliveryRating());
        review.setComment(request.comment());
        review.setIsVerified(true); // Verified because linked to completed order
        review.setCreatedAt(Instant.now());

        review = reviewRepository.save(review);

        LOGGER.info("Review created for order {} by customer {}", order.getId(), currentUser.id());

        return toView(review);
    }

    @Override
    public List<ReviewView> getStoreReviews(final Long storeId) {
        if (storeId == null) throw new IllegalArgumentException();

        return reviewRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewView> getCustomerReviews(final Long customerId) {
        if (customerId == null) throw new IllegalArgumentException();

        // Security check
        final var currentUser = currentUserProvider.requireCurrentUser();
        if (!Long.valueOf(currentUser.id()).equals(customerId) && currentUser.type() != PersonType.ADMIN) {
            throw new SecurityException("Cannot view other customer's reviews");
        }

        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReviewView> getReviewByOrder(final Long orderId) {
        if (orderId == null) throw new IllegalArgumentException();

        return reviewRepository.findByOrderId(orderId)
                .map(this::toView);
    }

    @Override
    public boolean hasOrderBeenReviewed(final Long orderId) {
        if (orderId == null) throw new IllegalArgumentException();
        return reviewRepository.existsByOrderId(orderId);
    }

    @Override
    public StoreRatingsSummary getStoreRatingsSummary(final Long storeId) {
        if (storeId == null) throw new IllegalArgumentException();

        Double avgRating = reviewRepository.calculateAverageRatingForStore(storeId);
        Long totalReviews = reviewRepository.countReviewsForStore(storeId);
        Double avgFoodRating = reviewRepository.calculateAverageFoodRating(storeId);
        Double avgDeliveryRating = reviewRepository.calculateAverageDeliveryRating(storeId);

        return new StoreRatingsSummary(
                storeId,
                avgRating,
                totalReviews,
                avgFoodRating,
                avgDeliveryRating
        );
    }

    @Transactional
    @Override
    public ReviewView addOwnerResponse(final Long reviewId, final String response) {
        if (reviewId == null) throw new IllegalArgumentException();
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Response cannot be empty");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Security: Only store owner can respond
        final var currentUser = currentUserProvider.requireCurrentUser();
        if (!review.getStore().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("Only store owner can respond to reviews");
        }

        review.setStoreReply(response);
        review.setStoreRepliedAt(Instant.now());
        review = reviewRepository.save(review);

        LOGGER.info("Owner response added to review {} by user {}", reviewId, currentUser.id());

        return toView(review);
    }

    @Override
    public Optional<ReviewView> getReview(final Long reviewId) {
        if (reviewId == null) throw new IllegalArgumentException();

        return reviewRepository.findById(reviewId)
                .map(this::toView);
    }

    private ReviewView toView(Review review) {
        if (review == null) return null;

        return new ReviewView(
                review.getId(),
                review.getStore() != null ? review.getStore().getId() : null,
                review.getStore() != null ? review.getStore().getName() : null,
                review.getCustomer() != null ? review.getCustomer().getId() : null,
                review.getCustomer() != null ? review.getCustomer().getFullName() : null,
                review.getOrder() != null ? review.getOrder().getId() : null,
                review.getRating(),
                review.getFoodRating(),
                review.getDeliveryRating(),
                review.getComment(),
                review.getStoreReply(),
                review.getStoreRepliedAt(),
                review.getIsVerified(),
                review.getCreatedAt()
        );
    }
}
