package com.streetfoodgo.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Represents a customer's selected customization for an order item.
 * Links the order item to the specific choices they made.
 */
@Entity
@Table(
        name = "order_item_customization",
        indexes = {
                @Index(name = "idx_customization_order_item", columnList = "order_item_id"),
                @Index(name = "idx_customization_choice", columnList = "menu_item_choice_id")
        }
)
public class OrderItemCustomization {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customization_order_item"))
    private OrderItem orderItem;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_choice_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customization_choice"))
    private MenuItemChoice menuItemChoice;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName; // Snapshot: e.g., "Size"

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "choice_name", nullable = false, length = 100)
    private String choiceName; // Snapshot: e.g., "Large"

    @NotNull
    @Column(name = "additional_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalPrice; // Snapshot of price at order time

    // Constructors
    public OrderItemCustomization() {
    }

    public OrderItemCustomization(OrderItem orderItem, MenuItemChoice choice) {
        this.orderItem = orderItem;
        this.menuItemChoice = choice;
        this.optionName = choice.getOption().getName();
        this.choiceName = choice.getName();
        this.additionalPrice = choice.getAdditionalPrice();
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

    public MenuItemChoice getMenuItemChoice() {
        return menuItemChoice;
    }

    public void setMenuItemChoice(MenuItemChoice menuItemChoice) {
        this.menuItemChoice = menuItemChoice;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getChoiceName() {
        return choiceName;
    }

    public void setChoiceName(String choiceName) {
        this.choiceName = choiceName;
    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public BigDecimal getTotalAdditionalPrice() {
        return additionalPrice;
    }
}
