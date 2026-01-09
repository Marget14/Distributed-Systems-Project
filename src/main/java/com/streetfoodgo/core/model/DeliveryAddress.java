package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * DeliveryAddress entity for customer delivery locations.
 */
@Entity
@Table(
        name = "delivery_address",
        indexes = {
                @Index(name = "idx_delivery_address_customer", columnList = "customer_id"),
                @Index(name = "idx_delivery_address_default", columnList = "is_default")
        }
)
public final class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_address_customer"))
    private Person customer;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "label", nullable = false, length = 100)
    private String label; // e.g., "Home", "Work", "Parents' House"

    @NotNull
    @NotBlank
    @Size(max = 500)
    @Column(name = "street", nullable = false, length = 500)
    private String street;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotNull
    @NotBlank
    @Size(max = 20)
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Size(max = 18)
    @Column(name = "phone_number", length = 18)
    private String phoneNumber; // Optional alternative phone for delivery

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public DeliveryAddress() {}

    public DeliveryAddress(Long id, Person customer, String label, String street, String city,
                           String postalCode, String phoneNumber, Double latitude, Double longitude,
                           Boolean isDefault, Instant createdAt) {
        this.id = id;
        this.customer = customer;
        this.label = label;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Person getCustomer() { return customer; }
    public void setCustomer(Person customer) { this.customer = customer; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getFullAddress() {
        return street + ", " + city + " " + postalCode;
    }

    @Override
    public String toString() {
        return "DeliveryAddress{id=" + id + ", label='" + label + "', city='" + city + "'}";
    }
}