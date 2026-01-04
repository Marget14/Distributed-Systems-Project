package com.streetfoodgo.core.repository;

import com.streetfoodgo.core.model.MenuCategory;
import com.streetfoodgo.core.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link MenuItem} entity.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findAllByStoreId(Long storeId);

    List<MenuItem> findAllByStoreIdAndAvailableTrue(Long storeId);

    List<MenuItem> findAllByStoreIdAndCategory(Long storeId, MenuCategory category);

    List<MenuItem> findAllByStoreIdAndCategoryAndAvailableTrue(Long storeId, MenuCategory category);
}