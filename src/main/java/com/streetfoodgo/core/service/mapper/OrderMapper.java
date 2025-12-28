package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.Order;

import com.streetfoodgo.core.service.model.OrderView;

import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Order} to {@link OrderView}.
 */
@Component
public class OrderMapper {

    private final PersonMapper personMapper;

    public OrderMapper(final PersonMapper personMapper) {
        if (personMapper == null) throw new NullPointerException();
        this.personMapper = personMapper;
    }

    public OrderView convertOrderToOrderView(final Order order) {
        if (order == null) {
            return null;
        }
        return new OrderView(
            order.getId(),
            this.personMapper.convertPersonToPersonView(order.getCustomer()),
            this.personMapper.convertPersonToPersonView(order.getWaiter()),
                order.getStatus(),
                order.getSubject(),
                order.getCustomerContent(),
                order.getWaiterContent(),
                order.getQueuedAt(),
                order.getInProgressAt(),
                order.getCompletedAt()
        );
    }
}
