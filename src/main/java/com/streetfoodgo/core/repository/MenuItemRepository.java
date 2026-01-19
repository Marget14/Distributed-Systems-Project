package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link MenuItem} entity.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Step 1: Load MenuItem with store
    @Query("SELECT m FROM MenuItem m " +
           "LEFT JOIN FETCH m.store " +
           "WHERE m.id = :id")
    Optional<MenuItem> findByIdWithStore(@Param("id") Long id);
    
    // Step 2: Load options for a MenuItem
    @Query("SELECT m FROM MenuItem m " +
           "LEFT JOIN FETCH m.options " +
           "WHERE m.id = :id")
    Optional<MenuItem> findByIdWithOptions(@Param("id") Long id);
    
    // Step 3: Load choices for each option (must be separate to avoid MultipleBagFetchException)
    @Query("SELECT o FROM MenuItemOption o " +
           "LEFT JOIN FETCH o.choices " +
           "WHERE o.menuItem.id = :menuItemId")
    List<com.streetfoodgo.core.model.MenuItemOption> findOptionsWithChoicesByMenuItemId(@Param("menuItemId") Long menuItemId);
    
    // Step 4: Load ingredients
    @Query("SELECT m FROM MenuItem m " +
           "LEFT JOIN FETCH m.ingredients " +
           "WHERE m.id = :id")
    Optional<MenuItem> findByIdWithIngredients(@Param("id") Long id);

    List<MenuItem> findAllByStoreId(Long storeId);

    List<MenuItem> findAllByStoreIdAndAvailableTrue(Long storeId);

    List<MenuItem> findAllByStoreIdAndCategory(Long storeId, MenuCategory category);

    List<MenuItem> findAllByStoreIdAndCategoryAndAvailableTrue(Long storeId, MenuCategory category);
}