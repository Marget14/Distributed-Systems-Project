package com.streetfoodgo.core.service.model;

/**
 * Summary of store ratings.
 */
public record StoreRatingsSummary(
        Long storeId,
        Double averageRating,
        Long totalReviews,
        Double averageFoodRating,
        Double averageDeliveryRating
) {
    public String getFormattedRating() {
        if (averageRating == null) return "No reviews yet";
        return String.format("%.1f", averageRating);
    }
}
