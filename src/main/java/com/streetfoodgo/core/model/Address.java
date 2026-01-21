package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Address entity representing delivery/pickup locations for users.
 * Customers can save multiple addresses for quick checkout.
 */
@Entity
@Table(
        name = "addresses",
        indexes = {
                @Index(name = "idx_address_user", columnList = "user_id"),
                @Index(name = "idx_address_default", columnList = "is_default")
        }
)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_user"))
    private Person user;

    @NotNull
    @NotBlank
    @Size(max = 50)
    @Column(name = "label", nullable = false, length = 50)
    private String label; // e.g., "Home", "Work", "Gym"

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "street", nullable = false, length = 100)
    private String street; // Street name and number

    @Size(max = 10)
    @Column(name = "street_number", length = 10)
    private String streetNumber;

    @Size(max = 5)
    @Column(name = "floor", length = 5)
    private String floor; // e.g., "3", "G" (ground floor)

    @Size(max = 10)
    @Column(name = "apartment_number", length = 10)
    private String apartmentNumber; // e.g., "A", "12"

    @NotNull
    @NotBlank
    @Size(max = 50)
    @Column(name = "area", nullable = false, length = 50)
    private String area; // e.g., "Kolonaki", "Syntagma"

    @NotNull
    @NotBlank
    @Size(max = 50)
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @NotNull
    @NotBlank
    @Size(max = 10)
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Size(max = 500)
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions; // "Ring twice", "Call on arrival", etc.

    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;


    // Constructors
    public Address() {}

    public Address(String label, String street, String city, String postalCode, Person user) {
        this.label = label;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Person getUser() { return user; }
    public void setUser(Person user) { this.user = user; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }


    // Helper methods
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null) sb.append(street);
        if (streetNumber != null) sb.append(" ").append(streetNumber);
        if (floor != null) sb.append(", Floor ").append(floor);
        if (apartmentNumber != null) sb.append("/").append(apartmentNumber);
        sb.append(", ").append(area).append(", ").append(city).append(" ").append(postalCode);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id) && Objects.equals(user, address.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", area='" + area + '\'' +
                ", city='" + city + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
