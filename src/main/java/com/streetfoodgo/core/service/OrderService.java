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

    List<OrderView> searchCustomerOrders(Long customerId, OrderSearchCriteria criteria);

    OrderStatistics getCustomerOrderStatistics(Long customerId);

    void cancelOrder(Long orderId);

    // Store owner operations
    List<OrderView> getStoreOrders(Long storeId);

    List<OrderView> getStoreOrdersByStatus(Long storeId, OrderStatus status);

    List<OrderView> searchStoreOrders(Long storeId, OrderSearchCriteria criteria);

    OrderStatistics getStoreOrderStatistics(Long storeId);

    OrderView acceptOrder(AcceptOrderRequest request);

    OrderView rejectOrder(RejectOrderRequest request);

    OrderView updateOrderStatus(UpdateOrderStatusRequest request);

    // Common operations
    Optional<OrderView> getOrder(Long id);

    List<OrderView> getAllOrders();

    void updateDriverLocation(Long orderId, Double latitude, Double longitude);
}