package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.CuisineType;
import com.streetfoodgo.core.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Store} entity.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByOwnerId(Long ownerId);

    List<Store> findAllByIsOpenTrue();

    List<Store> findAllByCuisineType(CuisineType cuisineType);

    List<Store> findAllByAreaIgnoreCase(String area);

    List<Store> findAllByCuisineTypeAndIsOpenTrue(CuisineType cuisineType);

    @Query("SELECT s FROM Store s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Store> searchByKeyword(@Param("keyword") String keyword);
}