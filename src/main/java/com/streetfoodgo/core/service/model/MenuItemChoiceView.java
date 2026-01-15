package com.streetfoodgo.core.service.model;

import java.math.BigDecimal;

/**
 * View model for menu item choice.
 */
public record MenuItemChoiceView(
        Long id,
        String name,
        String description,
        BigDecimal additionalPrice,
        Boolean isAvailable,
        Boolean isDefault,
        Integer displayOrder
) {
}
