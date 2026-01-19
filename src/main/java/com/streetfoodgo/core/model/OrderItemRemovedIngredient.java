package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Tracks ingredients that were removed from an order item.
 */
@Entity
@Table(
        name = "order_item_removed_ingredient",
        indexes = {
                @Index(name = "idx_removed_ingredient_order_item", columnList = "order_item_id")
        }
)
public class OrderItemRemovedIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_removed_ingredient_order_item"))
    private OrderItem orderItem;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "ingredient_name", nullable = false, length = 100)
    private String ingredientName; // Snapshot at order time

    // Constructors
    public OrderItemRemovedIngredient() {
    }

    public OrderItemRemovedIngredient(OrderItem orderItem, String ingredientName) {
        this.orderItem = orderItem;
        this.ingredientName = ingredientName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }
}
