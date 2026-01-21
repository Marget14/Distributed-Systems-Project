package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Ingredient entity representing individual customization choices within an IngredientGroup.
 *
 * Examples:
 * - Size group: "Regular" (€0), "Large" (+€2.00), "XL" (+€3.50)
 * - Extras group: "Extra Cheese" (+€1.00), "Bacon" (+€1.50)
 * - Removals: "No Onions" (€0), "No Pickles" (€0)
 */
@Entity
@Table(
        name = "ingredients",
        indexes = {
                @Index(name = "idx_ingredient_group", columnList = "ingredient_group_id")
        }
)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Large", "Extra Cheese", "No Onions"

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "price_adjustment", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAdjustment = BigDecimal.ZERO; // Can be positive (extra cost) or negative (discount)

    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false; // Pre-selected by default

    @NotNull
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingredient_group"))
    private IngredientGroup ingredientGroup;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public Ingredient() {}

    public Ingredient(String name, BigDecimal priceAdjustment, IngredientGroup ingredientGroup) {
        this.name = name;
        this.priceAdjustment = priceAdjustment;
        this.ingredientGroup = ingredientGroup;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPriceAdjustment() { return priceAdjustment; }
    public void setPriceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public IngredientGroup getIngredientGroup() { return ingredientGroup; }
    public void setIngredientGroup(IngredientGroup ingredientGroup) { this.ingredientGroup = ingredientGroup; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public String getDisplayName() {
        String name = this.name;
        if (priceAdjustment.signum() > 0) {
            name += " (+€" + priceAdjustment.setScale(2, java.math.RoundingMode.HALF_UP) + ")";
        } else if (priceAdjustment.signum() < 0) {
            name += " (-€" + priceAdjustment.abs().setScale(2, java.math.RoundingMode.HALF_UP) + ")";
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(ingredientGroup, that.ingredientGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ingredientGroup);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priceAdjustment=" + priceAdjustment +
                ", isDefault=" + isDefault +
                ", isAvailable=" + isAvailable +
                ", displayOrder=" + displayOrder +
                '}';
    }
}
