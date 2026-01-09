package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.MenuItem;
import com.streetfoodgo.core.service.model.MenuItemView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert MenuItem entity to MenuItemView DTO.
 */
@Component
public class MenuItemMapper {

    public MenuItemView toView(final MenuItem menuItem) {
        if (menuItem == null) return null;

        return new MenuItemView(
                menuItem.getId(),
                menuItem.getStore().getId(),
                menuItem.getStore().getName(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getAvailable(),
                menuItem.getImageUrl(),
                menuItem.getCreatedAt()
        );
    }
}