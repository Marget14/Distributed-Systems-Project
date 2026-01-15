package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.service.ReviewService;
import com.streetfoodgo.core.service.model.CreateReviewRequest;
import com.streetfoodgo.core.service.model.ReviewView;
import com.streetfoodgo.core.service.model.StoreRatingsSummary;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for reviews and ratings.
 */
@RestController
@RequestMapping(value = "/api/v1/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReviewResource {

    private final ReviewService reviewService;

    public ReviewResource(final ReviewService reviewService) {
        if (reviewService == null) throw new NullPointerException();
        this.reviewService = reviewService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ReviewView> createReview(@RequestBody @Valid CreateReviewRequest request) {
        final ReviewView created = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/store/{storeId}")
    public List<ReviewView> getStoreReviews(@PathVariable Long storeId) {
        return reviewService.getStoreReviews(storeId);
    }

    @GetMapping("/store/{storeId}/summary")
    public StoreRatingsSummary getStoreRatingsSummary(@PathVariable Long storeId) {
        return reviewService.getStoreRatingsSummary(storeId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public List<ReviewView> getCustomerReviews(@PathVariable Long customerId) {
        return reviewService.getCustomerReviews(customerId);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReviewView> getReviewByOrder(@PathVariable Long orderId) {
        return reviewService.getReviewByOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{reviewId}/response")
    public ResponseEntity<ReviewView> addOwnerResponse(
            @PathVariable Long reviewId,
            @RequestBody String response) {
        
        final ReviewView updated = reviewService.addOwnerResponse(reviewId, response);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewView> getReview(@PathVariable Long reviewId) {
        return reviewService.getReview(reviewId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
