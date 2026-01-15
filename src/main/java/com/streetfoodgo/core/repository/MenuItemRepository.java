package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.model.MenuItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link MenuItem} entity.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @EntityGraph(attributePaths = {"store", "options", "options.choices", "ingredients"})
    Optional<MenuItem> findWithDetailsById(Long id);

    List<MenuItem> findAllByStoreId(Long storeId);

    List<MenuItem> findAllByStoreIdAndAvailableTrue(Long storeId);

    List<MenuItem> findAllByStoreIdAndCategory(Long storeId, MenuCategory category);

    List<MenuItem> findAllByStoreIdAndCategoryAndAvailableTrue(Long storeId, MenuCategory category);
}