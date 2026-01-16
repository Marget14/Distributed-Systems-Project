package com.streetfoodgo.web.api.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A single line in the session cart.
 *
 * Important: Unlike the previous Map<menuItemId, CartItem>, this supports multiple lines
 * for the same menu item (different customizations).
 */
public class CartLine {

    private String lineId; // UUID string
    private Long menuItemId;
    private Long storeId;
    private String name;
    private BigDecimal price;
    private int quantity;

    private List<Long> selectedChoiceIds = new ArrayList<>();
    private List<Long> removedIngredientIds = new ArrayList<>();
    private String specialInstructions;

    public CartLine() {}

    public CartLine(
            final String lineId,
            final Long menuItemId,
            final Long storeId,
            final String name,
            final BigDecimal price,
            final int quantity) {
        this.lineId = lineId;
        this.menuItemId = menuItemId;
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public List<Long> getSelectedChoiceIds() { return selectedChoiceIds; }
    public void setSelectedChoiceIds(List<Long> selectedChoiceIds) { this.selectedChoiceIds = selectedChoiceIds; }

    public List<Long> getRemovedIngredientIds() { return removedIngredientIds; }
    public void setRemovedIngredientIds(List<Long> removedIngredientIds) { this.removedIngredientIds = removedIngredientIds; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    /**
     * Used to decide whether we can merge two lines (same menu item + same customization).
     */
    public boolean hasSameCustomizationAs(final CartLine other) {
        if (other == null) return false;
        return Objects.equals(this.menuItemId, other.menuItemId)
                && Objects.equals(this.sorted(this.selectedChoiceIds), this.sorted(other.selectedChoiceIds))
                && Objects.equals(this.sorted(this.removedIngredientIds), this.sorted(other.removedIngredientIds))
                && Objects.equals(normalize(this.specialInstructions), normalize(other.specialInstructions));
    }

    private List<Long> sorted(final List<Long> ids) {
        if (ids == null) return List.of();
        return ids.stream().filter(Objects::nonNull).sorted().toList();
    }

    private static String normalize(final String s) {
        if (s == null) return null;
        final String trimmed = s.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    /**
     * Calculate total price for this cart line (price * quantity).
     */
    public BigDecimal getTotalPrice() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
