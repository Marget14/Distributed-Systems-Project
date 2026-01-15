package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "review",
        indexes = {
                @Index(name = "idx_review_store", columnList = "store_id"),
                @Index(name = "idx_review_customer", columnList = "customer_id"),
                @Index(name = "idx_review_order", columnList = "order_id"),
                @Index(name = "idx_review_rating", columnList = "rating")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_review_order", columnNames = "order_id")
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_review_order"))
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", foreignKey = @ForeignKey(name = "fk_review_store"))
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_review_customer"))
    private Person customer;

    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Min(1)
    @Max(5)
    @Column(name = "food_rating")
    private Integer foodRating;

    @Min(1)
    @Max(5)
    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    @Size(max = 2000)
    @Column(name = "comment", length = 2000)
    private String comment;

    @Size(max = 2000)
    @Column(name = "store_reply", length = 2000)
    private String storeReply;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "store_replied_at")
    private Instant storeRepliedAt;

    public Review(Order order, Store store, Person customer, Integer rating, String comment, String storeReply, Instant createdAt, Instant storeRepliedAt) {
        this.order = order;
        this.store = store;
        this.customer = customer;
        this.rating = rating;
        this.comment = comment;
        this.storeReply = storeReply;
        this.createdAt = createdAt;
        this.storeRepliedAt = storeRepliedAt;
    }

    public Review() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Person getCustomer() {
        return customer;
    }

    public void setCustomer(Person customer) {
        this.customer = customer;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStoreReply() {
        return storeReply;
    }

    public void setStoreReply(String storeReply) {
        this.storeReply = storeReply;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStoreRepliedAt() {
        return storeRepliedAt;
    }

    public void setStoreRepliedAt(Instant storeRepliedAt) {
        this.storeRepliedAt = storeRepliedAt;
    }

    public Integer getFoodRating() { return foodRating; }
    public void setFoodRating(Integer foodRating) { this.foodRating = foodRating; }

    public Integer getDeliveryRating() { return deliveryRating; }
    public void setDeliveryRating(Integer deliveryRating) { this.deliveryRating = deliveryRating; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean verified) { isVerified = verified; }
}