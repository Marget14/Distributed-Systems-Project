package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.repository.OrderRepository;
import com.streetfoodgo.core.service.OrderDataService;
import com.streetfoodgo.core.service.mapper.OrderMapper;
import com.streetfoodgo.core.service.model.OrderView;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link OrderDataService}.
 */
@Service
public class OrderDataServiceImpl implements OrderDataService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderDataServiceImpl(final OrderRepository orderRepository,
                                final OrderMapper orderMapper) {
        if (orderRepository == null) throw new NullPointerException();
        if (orderMapper == null) throw new NullPointerException();
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public List<OrderView> getAllOrders() {
        final List<Order> orderList = this.orderRepository.findAll();
        final List<OrderView> orderViewList = orderList
            .stream()
            .map(this.orderMapper::convertOrderToOrderView)
            .toList();
        return orderViewList;
    }
}
