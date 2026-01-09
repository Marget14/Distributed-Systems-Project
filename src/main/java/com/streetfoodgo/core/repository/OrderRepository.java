package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Repository for {@link Order} entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByCustomerId(Long customerId);

    List<Order> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findAllByStoreId(Long storeId);

    List<Order> findAllByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<Order> findAllByStoreIdAndStatus(Long storeId, OrderStatus status);

    List<Order> findAllByStatus(OrderStatus status);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant before);

    long countByCustomerIdAndStatusIn(Long customerId, Collection<OrderStatus> statuses);

    long countByStoreIdAndStatus(Long storeId, OrderStatus status);
}