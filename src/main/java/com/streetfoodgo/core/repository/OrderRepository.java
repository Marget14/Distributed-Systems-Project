package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.model.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId AND o.status = :status")
    Long countByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:orderType IS NULL OR o.orderType = :orderType) " +
           "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
           "ORDER BY o.createdAt DESC")
    List<Order> findCustomerOrdersWithFilters(
            @Param("customerId") Long customerId,
            @Param("status") OrderStatus status,
            @Param("orderType") OrderType orderType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:orderType IS NULL OR o.orderType = :orderType) " +
           "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
           "ORDER BY o.createdAt DESC")
    List<Order> findStoreOrdersWithFilters(
            @Param("storeId") Long storeId,
            @Param("status") OrderStatus status,
            @Param("orderType") OrderType orderType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}