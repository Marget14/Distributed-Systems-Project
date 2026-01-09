package com.streetfoodgo.core.service;

import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.service.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Order business logic.
 */
public interface OrderService {

    // Customer operations
    OrderView createOrder(CreateOrderRequest request);

    List<OrderView> getCustomerOrders(Long customerId);

    void cancelOrder(Long orderId);

    // Store owner operations
    List<OrderView> getStoreOrders(Long storeId);

    List<OrderView> getStoreOrdersByStatus(Long storeId, OrderStatus status);

    OrderView acceptOrder(AcceptOrderRequest request);

    OrderView rejectOrder(RejectOrderRequest request);

    OrderView updateOrderStatus(UpdateOrderStatusRequest request);

    // Common operations
    Optional<OrderView> getOrder(Long id);

    List<OrderView> getAllOrders();
}