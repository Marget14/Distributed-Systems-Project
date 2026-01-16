package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.OrderItem;
import com.streetfoodgo.core.service.model.OrderItemView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert OrderItem entity to OrderItemView DTO.
 */
@Component
public class OrderItemMapper {

    private final MenuItemMapper menuItemMapper;

    public OrderItemMapper(final MenuItemMapper menuItemMapper) {
        if (menuItemMapper == null) throw new NullPointerException();
        this.menuItemMapper = menuItemMapper;
    }

    public OrderItemView toView(final OrderItem orderItem) {
        if (orderItem == null) return null;

        return new OrderItemView(
                orderItem.getId(),
                menuItemMapper.toView(orderItem.getMenuItem()),
                orderItem.getQuantity(),
                orderItem.getPriceAtOrder(),
                orderItem.getSpecialInstructions()
        );
    }
}