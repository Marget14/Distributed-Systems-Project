package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.service.model.OrderView;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper to convert Order entity to OrderView DTO.
 */
@Component
public class OrderMapper {

    private final PersonMapper personMapper;
    private final StoreMapper storeMapper;
    private final DeliveryAddressMapper deliveryAddressMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderMapper(final PersonMapper personMapper,
                       final StoreMapper storeMapper,
                       final DeliveryAddressMapper deliveryAddressMapper,
                       final OrderItemMapper orderItemMapper) {
        if (personMapper == null) throw new NullPointerException();
        if (storeMapper == null) throw new NullPointerException();
        if (deliveryAddressMapper == null) throw new NullPointerException();
        if (orderItemMapper == null) throw new NullPointerException();

        this.personMapper = personMapper;
        this.storeMapper = storeMapper;
        this.deliveryAddressMapper = deliveryAddressMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public OrderView toView(final Order order) {
        if (order == null) return null;

        return new OrderView(
                order.getId(),
                personMapper.toView(order.getCustomer()),
                storeMapper.toView(order.getStore()),
                order.getOrderType(),
                deliveryAddressMapper.toView(order.getDeliveryAddress()),
                order.getItems().stream()
                        .map(orderItemMapper::toView)
                        .collect(Collectors.toList()),
                order.getStatus(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getTotal(),
                order.getCustomerNotes(),
                order.getRejectionReason(),
                order.getCreatedAt(),
                order.getAcceptedAt(),
                order.getReadyAt(),
                order.getDeliveringAt(),
                order.getCompletedAt(),
                order.getRejectedAt(),
                order.getCancelledAt(),
                order.getEstimatedDeliveryMinutes(),
                order.getEstimatedDeliveryDistanceKm()
        );
    }
}