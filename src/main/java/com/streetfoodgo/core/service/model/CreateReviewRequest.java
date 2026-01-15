package com.streetfoodgo.core.service.model;

import jakarta.validation.constraints.*;

/**
 * Request to create a review.
 */
public record CreateReviewRequest(
        @NotNull @Positive Long orderId,
        @NotNull @Min(1) @Max(5) Integer rating,
        @Min(1) @Max(5) Integer foodRating,
        @Min(1) @Max(5) Integer deliveryRating,
        @Size(max = 2000) String comment
) {
}
