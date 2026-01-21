package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * IngredientGroup represents a customization group for menu items.
 * Examples: "Choose your size", "Add extras", "Choose sauce", "Remove ingredients"
 *
 * A menu item can have multiple ingredient groups, and customers must select ingredients
 * from each group according to the group's constraints (required, min/max selections, etc.)
 */
@Entity
@Table(
        name = "ingredient_groups",
        indexes = {
                @Index(name = "idx_ingredient_group_menu_item", columnList = "menu_item_id")
        }
)
public class IngredientGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Μέγεθος", "Πρόσθετα", "Αποφύγετε"

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false, length = 20)
    private IngredientGroupSelectionType selectionType = IngredientGroupSelectionType.SINGLE_SELECT;

    @NotNull
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false; // true = customer MUST select something

    @NotNull
    @Min(0)
    @Column(name = "min_selections", nullable = false)
    private Integer minSelections = 0;

    @NotNull
    @Min(1)
    @Column(name = "max_selections", nullable = false)
    private Integer maxSelections = 1;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingredient_group_menu_item"))
    private MenuItem menuItem;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Relationships
    @OneToMany(mappedBy = "ingredientGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<Ingredient> ingredients = new ArrayList<>();

    // Constructors
    public IngredientGroup() {}

    public IngredientGroup(String name, MenuItem menuItem) {
        this.name = name;
        this.menuItem = menuItem;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public IngredientGroupSelectionType getSelectionType() { return selectionType; }
    public void setSelectionType(IngredientGroupSelectionType selectionType) { this.selectionType = selectionType; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public Integer getMinSelections() { return minSelections; }
    public void setMinSelections(Integer minSelections) { this.minSelections = minSelections; }

    public Integer getMaxSelections() { return maxSelections; }
    public void setMaxSelections(Integer maxSelections) { this.maxSelections = maxSelections; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }

    // Helper methods
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setIngredientGroup(this);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setIngredientGroup(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngredientGroup that = (IngredientGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IngredientGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", selectionType=" + selectionType +
                ", isRequired=" + isRequired +
                ", minSelections=" + minSelections +
                ", maxSelections=" + maxSelections +
                '}';
    }
}
