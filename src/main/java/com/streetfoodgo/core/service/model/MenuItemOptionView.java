package com.streetfoodgo.core.service.model;

import java.util.List;

/**
 * View model for menu item option.
 */
public record MenuItemOptionView(
        Long id,
        String name,
        String description,
        Boolean isRequired,
        Boolean allowMultiple,
        Integer minSelections,
        Integer maxSelections,
        Integer displayOrder,
        List<MenuItemChoiceView> choices
) {
}
