package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link DeliveryAddress} entity.
 */
@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {

    List<DeliveryAddress> findAllByCustomerId(Long customerId);

    List<DeliveryAddress> findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(Long customerId);

    Optional<DeliveryAddress> findByCustomerIdAndIsDefaultTrue(Long customerId);

    long countByCustomerId(Long customerId);

    @Modifying
    @Query("UPDATE DeliveryAddress da SET da.isDefault = false WHERE da.customer.id = :customerId")
    void resetDefaultAddress(Long customerId);
}