package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * OrderItem entity representing an item in an order.
 */
@Entity
@Table(
        name = "order_item",
        indexes = {
                @Index(name = "idx_order_item_order", columnList = "order_id"),
                @Index(name = "idx_order_item_menu_item", columnList = "menu_item_id")
        }
)
public final class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_order"))
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_menu_item"))
    private MenuItem menuItem;

    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "price_at_order", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtOrder; // Price snapshot at time of order

    @Size(max = 500)
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions; // e.g., "No onions", "Extra cheese"

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderItemCustomization> customizations = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderItemRemovedIngredient> removedIngredients = new java.util.ArrayList<>();

    // Constructors
    public OrderItem() {}

    public OrderItem(Long id, Order order, MenuItem menuItem, Integer quantity,
                     BigDecimal priceAtOrder, String specialInstructions) {
        this.id = id;
        this.order = order;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
        this.specialInstructions = specialInstructions;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(BigDecimal priceAtOrder) { this.priceAtOrder = priceAtOrder; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public java.util.List<OrderItemCustomization> getCustomizations() { return customizations; }
    public void setCustomizations(java.util.List<OrderItemCustomization> customizations) { this.customizations = customizations; }

    public java.util.List<OrderItemRemovedIngredient> getRemovedIngredients() { return removedIngredients; }
    public void setRemovedIngredients(java.util.List<OrderItemRemovedIngredient> removedIngredients) { 
        this.removedIngredients = removedIngredients; 
    }

    public void addCustomization(OrderItemCustomization customization) {
        customizations.add(customization);
        customization.setOrderItem(this);
    }

    public void addRemovedIngredient(OrderItemRemovedIngredient removedIngredient) {
        removedIngredients.add(removedIngredient);
        removedIngredient.setOrderItem(this);
    }

    // Utility methods
    public BigDecimal getSubtotal() {
        BigDecimal basePrice = priceAtOrder.multiply(BigDecimal.valueOf(quantity));
        BigDecimal customizationPrice = customizations.stream()
                .map(OrderItemCustomization::getAdditionalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(quantity));
        return basePrice.add(customizationPrice);
    }

    @Override
    public String toString() {
        return "OrderItem{id=" + id + ", quantity=" + quantity + ", price=" + priceAtOrder + '}';
    }
}