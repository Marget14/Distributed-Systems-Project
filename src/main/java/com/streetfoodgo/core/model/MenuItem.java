package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MenuItem entity representing items in a store's menu.
 */
@Entity
@Table(
        name = "menu_item",
        indexes = {
                @Index(name = "idx_menu_item_store", columnList = "store_id"),
                @Index(name = "idx_menu_item_category", columnList = "category"),
                @Index(name = "idx_menu_item_available", columnList = "available")
        }
)
public final class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_item_store"))
    private Store store;

    @NotNull
    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private MenuCategory category;

    @NotNull
    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private java.util.List<MenuItemOption> options = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private java.util.List<MenuItemIngredient> ingredients = new java.util.ArrayList<>();

    // Constructors
    public MenuItem() {}

    public MenuItem(Long id, Store store, String name, String description, BigDecimal price,
                    MenuCategory category, Boolean available, String imageUrl, Instant createdAt) {
        this.id = id;
        this.store = store;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = available;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public java.util.List<MenuItemOption> getOptions() { return options; }
    public void setOptions(java.util.List<MenuItemOption> options) { this.options = options; }

    public java.util.List<MenuItemIngredient> getIngredients() { return ingredients; }
    public void setIngredients(java.util.List<MenuItemIngredient> ingredients) { this.ingredients = ingredients; }

    public void addOption(MenuItemOption option) {
        options.add(option);
        option.setMenuItem(this);
    }

    public void removeOption(MenuItemOption option) {
        options.remove(option);
        option.setMenuItem(null);
    }

    public void addIngredient(MenuItemIngredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setMenuItem(this);
    }

    public void removeIngredient(MenuItemIngredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setMenuItem(null);
    }

    @Override
    public String toString() {
        return "MenuItem{id=" + id + ", name='" + name + "', price=" + price + '}';
    }
}