package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Store entity representing food establishments (restaurants, food trucks, etc.).
 */
@Entity
@Table(
        name = "store",
        indexes = {
                @Index(name = "idx_store_owner", columnList = "owner_id"),
                @Index(name = "idx_store_cuisine_type", columnList = "cuisine_type"),
                @Index(name = "idx_store_is_open", columnList = "is_open")
        }
)
public final class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_store_owner"))
    private Person owner;

    @NotNull
    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_type", nullable = false, length = 50)
    private CuisineType cuisineType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", nullable = false, length = 50)
    private StoreType storeType;

    // Location
    @NotNull
    @NotBlank
    @Size(max = 500)
    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Size(max = 100)
    @Column(name = "area", length = 100)
    private String area; // e.g., "Kolonaki", "Syntagma"

    // Business settings
    @Column(name = "opening_hours", length = 500)
    private String openingHours; // JSON or simple string format

    @NotNull
    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = true;

    @NotNull
    @Column(name = "minimum_order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "accepts_delivery", nullable = false)
    private Boolean acceptsDelivery = true;

    @NotNull
    @Column(name = "accepts_pickup", nullable = false)
    private Boolean acceptsPickup = true;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.valueOf(2.50);

    @Column(name = "estimated_delivery_time_minutes")
    private Integer estimatedDeliveryTimeMinutes = 30;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public Store() {}

    public Store(Long id, Person owner, String name, String description, CuisineType cuisineType,
                 StoreType storeType, String address, Double latitude, Double longitude, String area,
                 String openingHours, Boolean isOpen, BigDecimal minimumOrderAmount,
                 Boolean acceptsDelivery, Boolean acceptsPickup, BigDecimal deliveryFee,
                 Integer estimatedDeliveryTimeMinutes, String imageUrl, Instant createdAt) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.cuisineType = cuisineType;
        this.storeType = storeType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
        this.openingHours = openingHours;
        this.isOpen = isOpen;
        this.minimumOrderAmount = minimumOrderAmount;
        this.acceptsDelivery = acceptsDelivery;
        this.acceptsPickup = acceptsPickup;
        this.deliveryFee = deliveryFee;
        this.estimatedDeliveryTimeMinutes = estimatedDeliveryTimeMinutes;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Person getOwner() { return owner; }
    public void setOwner(Person owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CuisineType getCuisineType() { return cuisineType; }
    public void setCuisineType(CuisineType cuisineType) { this.cuisineType = cuisineType; }

    public StoreType getStoreType() { return storeType; }
    public void setStoreType(StoreType storeType) { this.storeType = storeType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }

    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) { this.minimumOrderAmount = minimumOrderAmount; }

    public Boolean getAcceptsDelivery() { return acceptsDelivery; }
    public void setAcceptsDelivery(Boolean acceptsDelivery) { this.acceptsDelivery = acceptsDelivery; }

    public Boolean getAcceptsPickup() { return acceptsPickup; }
    public void setAcceptsPickup(Boolean acceptsPickup) { this.acceptsPickup = acceptsPickup; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public Integer getEstimatedDeliveryTimeMinutes() { return estimatedDeliveryTimeMinutes; }
    public void setEstimatedDeliveryTimeMinutes(Integer estimatedDeliveryTimeMinutes) {
        this.estimatedDeliveryTimeMinutes = estimatedDeliveryTimeMinutes;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Store{id=" + id + ", name='" + name + "', cuisineType=" + cuisineType + '}';
    }
}