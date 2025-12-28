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

    List<Order> findAllByCustomerId(long customerId);

    List<Order> findAllByWaiterId(long waiterId);

    List<Order> findByStatusAndQueuedAtBefore(final OrderStatus status, Instant before);

    boolean existsByCustomerIdAndWaiterIdAndStatusIn(final long customerId, final long waiterId, final Collection<OrderStatus> statuses);

    long countByCustomerIdAndStatusIn(final long customerId, final Collection<OrderStatus> statuses);
}
