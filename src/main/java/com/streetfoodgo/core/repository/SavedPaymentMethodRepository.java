package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.SavedPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPaymentMethodRepository extends JpaRepository<SavedPaymentMethod, Long> {
    List<SavedPaymentMethod> findByPersonId(Long personId);
    List<SavedPaymentMethod> findByPersonIdOrderByCreatedAtDesc(Long personId);
}
