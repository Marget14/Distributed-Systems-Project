package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents an ingredient in a menu item.
 * Customers can see ingredients and optionally remove them.
 */
@Entity
@Table(
        name = "menu_item_ingredient",
        indexes = {
                @Index(name = "idx_ingredient_menu_item", columnList = "menu_item_id")
        }
)
public class MenuItemIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingredient_menu_item"))
    private MenuItem menuItem;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Tomatoes", "Lettuce", "Onions"

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "is_removable", nullable = false)
    private Boolean isRemovable = true; // Can customer remove this?

    @NotNull
    @Column(name = "is_allergen", nullable = false)
    private Boolean isAllergen = false; // Is this an allergen?

    @Size(max = 200)
    @Column(name = "allergen_info", length = 200)
    private String allergenInfo; // e.g., "Contains nuts", "Dairy"

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    // Constructors
    public MenuItemIngredient() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsRemovable() {
        return isRemovable;
    }

    public void setIsRemovable(Boolean removable) {
        isRemovable = removable;
    }

    public Boolean getIsAllergen() {
        return isAllergen;
    }

    public void setIsAllergen(Boolean allergen) {
        isAllergen = allergen;
    }

    public String getAllergenInfo() {
        return allergenInfo;
    }

    public void setAllergenInfo(String allergenInfo) {
        this.allergenInfo = allergenInfo;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
