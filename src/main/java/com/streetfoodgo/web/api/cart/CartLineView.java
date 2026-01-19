package com.streetfoodgo.web.api.cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO returned to the browser.
 */
public record CartLineView(
        String lineId,
        Long menuItemId,
        Long storeId,
        String name,
        BigDecimal price,
        int quantity,
        List<Long> selectedChoiceIds,
        List<Long> removedIngredientIds,
        String specialInstructions
) {
    public static CartLineView from(final CartLine line) {
        return new CartLineView(
                line.getLineId(),
                line.getMenuItemId(),
                line.getStoreId(),
                line.getName(),
                line.getPrice(),
                line.getQuantity(),
                line.getSelectedChoiceIds(),
                line.getRemovedIngredientIds(),
                line.getSpecialInstructions()
        );
    }
}
