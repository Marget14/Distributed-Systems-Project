package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.Address;
import com.streetfoodgo.core.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Address} entity.
 * Handles user address management and lookup.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all addresses for a user.
     * Used for address list display in profile.
     */
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(Person user);

    /**
     * Find default address for a user.
     * Used for quick checkout.
     */
    Optional<Address> findByUserAndIsDefaultTrue(Person user);

    /**
     * Find address by ID and user (security check).
     * Used to ensure user can only access their own addresses.
     */
    Optional<Address> findByIdAndUser(Long id, Person user);

    /**
     * Check if user has addresses.
     * Used to show "Add Address" prompt if empty.
     */
    boolean existsByUser(Person user);

    /**
     * Count user's addresses.
     */
    long countByUser(Person user);

    /**
     * Delete all addresses for a user.
     * Used when user account is deleted.
     */
    void deleteByUser(Person user);

    /**
     * Find addresses in a specific area.
     * Used for area-based delivery validation.
     */
    List<Address> findByUserAndArea(Person user, String area);
}
